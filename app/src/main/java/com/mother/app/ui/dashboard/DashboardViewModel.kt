package com.mother.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.di.AppContainer
import com.mother.app.data.local.entity.AppSettingEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.data.repository.SettingRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

/** UI state for the Dashboard. Single source of state (AGENT_RULES §6). */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val greeting: String = "",
    val dateLabel: String = "",
    val streak: Int = 0,
    val todayStudyMinutes: Int = 0,
    val dailyTargetMinutes: Int = 120,
    val nextActivity: ScheduleEntity? = null,
    val deadlines: List<TaskEntity> = emptyList(),
    val todaySchedule: List<ScheduleEntity> = emptyList()
)

/** Holds the five data flows combined for the dashboard. */
private data class DataHolder(
    val studyMin: Int,
    val schedules: List<ScheduleEntity>,
    val deadlines: List<TaskEntity>,
    val setting: AppSettingEntity?,
    val sessions: List<StudySessionEntity>
)

class DashboardViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val taskRepository: TaskRepository,
    private val studySessionRepository: StudySessionRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val now: () -> Long get() = { System.currentTimeMillis() }

    init {
        // Day boundaries. Re-derived on a single ticker so the dashboard rolls
        // over at midnight and the countdowns stay current (AGENT_RULES §11).
        val dayStart = MutableStateFlow(TimeUtils.todayStart())
        val dayEnd = MutableStateFlow(TimeUtils.todayEnd())

        viewModelScope.launch {
            while (true) {
                delay(1000)
                dayStart.value = TimeUtils.todayStart()
                dayEnd.value = TimeUtils.todayEnd()
            }
        }

        val studyFlow = studySessionRepository.observeTotalMinutesRange(dayStart.value, dayEnd.value)
        val scheduleFlow = scheduleRepository.observeForDay(dayStart.value, dayEnd.value)
        val deadlineFlow = taskRepository.observeUpcomingDeadlines(3)
        val settingFlow = settingRepository.observeSetting()
        val sessionsFlow = studySessionRepository.observeAllAsc()

        // Stage 1: combine the five data flows into a typed holder (typed overload).
        val dataFlow = combine(
            studyFlow, scheduleFlow, deadlineFlow, settingFlow, sessionsFlow
        ) { studyMin, schedules, deadlines, setting, sessions ->
            DataHolder(studyMin, schedules, deadlines, setting, sessions)
        }

        // Stage 2: combine with the day-boundary ticker to build the UI state.
        viewModelScope.launch {
            combine(dataFlow, dayStart, dayEnd) { data, _, _ ->
                val currentTime = now()
                DashboardUiState(
                    isLoading = false,
                    error = null,
                    greeting = greetingForHour(currentTime),
                    dateLabel = TimeUtils.formatFullDate(currentTime),
                    streak = computeStreak(data.sessions),
                    todayStudyMinutes = data.studyMin,
                    dailyTargetMinutes = data.setting?.defaultStudyTargetMinute ?: 120,
                    nextActivity = data.schedules.firstOrNull { it.startTime > currentTime },
                    deadlines = data.deadlines,
                    todaySchedule = data.schedules
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
        }
    }

    /** Re-triggers a state emission (used by the error state's retry action). */
    fun refresh() {
        _uiState.update { it.copy(isLoading = false) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DashboardViewModel(
                    scheduleRepository = container.scheduleRepository,
                    taskRepository = container.taskRepository,
                    studySessionRepository = container.studySessionRepository,
                    settingRepository = container.settingRepository
                )
            }
        }
    }

    private fun computeStreak(sessions: List<StudySessionEntity>): Int =
        TimeUtils.computeStreak(sessions.map { it.startTime })

    private fun greetingForHour(currentTime: Long): String {
        val hour = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentTime), java.time.ZoneId.systemDefault()).hour
        return when (hour) {
            in 5..11 -> "Selamat Pagi"
            in 12..15 -> "Selamat Siang"
            else -> "Selamat Malam"
        }
    }
}