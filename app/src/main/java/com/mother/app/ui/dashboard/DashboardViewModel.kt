package com.mother.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.R
import com.mother.app.data.local.entity.AppSettingEntity
import com.mother.app.data.local.entity.HabitEntity
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.HabitRepository
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.data.repository.SettingRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
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

/** Typed holder combining the data flows feeding the dashboard. */
private data class DashboardData(
    val studyMin: Int,
    val schedules: List<ScheduleEntity>,
    val deadlines: List<TaskEntity>,
    val setting: AppSettingEntity?,
    val sessions: List<StudySessionEntity>,
    val activeHabits: List<HabitEntity>
)

/**
 * Builds the dashboard state. Day boundaries are re-derived from a per-second
 * ticker so the screen rolls over at midnight and the countdown stays current
 * (PRD §10). The streak counts consecutive days with at least one study
 * session, ending today.
 */
class DashboardViewModel(
    application: Application,
    private val scheduleRepository: ScheduleRepository,
    private val taskRepository: TaskRepository,
    private val studySessionRepository: StudySessionRepository,
    private val settingRepository: SettingRepository,
    private val habitRepository: HabitRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    /** Per-second ticker driving the greeting, next activity, and date rollover. */
    private val tick = MutableStateFlow(System.currentTimeMillis())

    /** Re-observation trigger used by the error state's retry action. */
    private val retryTrigger = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tick.value = System.currentTimeMillis()
            }
        }

        val dayStart = tick.map { TimeUtils.startOfDay(it) }.distinctUntilChanged()

        // Re-query when the day rolls over (flatMapLatest) or after a retry;
        // failures surface as an error state instead of crashing (PRD §35).
        val dataFlow: Flow<DashboardData> = combine(dayStart, retryTrigger) { start, _ -> start }
            .flatMapLatest { start ->
                val end = TimeUtils.endOfDay(start)
                combine(
                    studySessionRepository.observeTotalMinutesRange(start, end),
                    scheduleRepository.observeForDay(start, end),
                    taskRepository.observeUpcomingDeadlines(3),
                    settingRepository.observeSetting(),
                    studySessionRepository.observeAllAsc(),
                    habitRepository.observeActive()
                ) { flows ->
                    @Suppress("UNCHECKED_CAST")
                    DashboardData(
                        studyMin = flows[0] as Int,
                        schedules = flows[1] as List<ScheduleEntity>,
                        deadlines = flows[2] as List<TaskEntity>,
                        setting = flows[3] as AppSettingEntity?,
                        sessions = flows[4] as List<StudySessionEntity>,
                        activeHabits = flows[5] as List<HabitEntity>
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Terjadi kesalahan") }
            }

        viewModelScope.launch {
            combine(dataFlow, tick) { data, currentTime ->
                val habitTargetSum = data.activeHabits.sumOf { it.targetMinute }
                val targetMinutes = if (habitTargetSum > 0) habitTargetSum else (data.setting?.defaultStudyTargetMinute ?: 120)

                DashboardUiState(
                    isLoading = false,
                    error = null,
                    greeting = greetingForHour(currentTime),
                    dateLabel = TimeUtils.formatFullDate(currentTime),
                    streak = TimeUtils.computeStreak(data.sessions.map { it.startTime }),
                    todayStudyMinutes = data.studyMin,
                    dailyTargetMinutes = targetMinutes,
                    nextActivity = data.schedules.firstOrNull { it.startTime > currentTime },
                    deadlines = data.deadlines,
                    todaySchedule = data.schedules
                )
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Terjadi kesalahan") }
                }
                .collect { _uiState.value = it }
        }
    }

    /** Re-observes the data after an error (used by the error state's retry). */
    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        retryTrigger.update { it + 1 }
    }

    private fun greetingForHour(currentTime: Long): String {
        val hour = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), ZoneId.systemDefault()).hour
        val resources = getApplication<Application>().resources
        return when (hour) {
            in 5..11 -> resources.getString(R.string.dashboard_greeting_morning)
            in 12..15 -> resources.getString(R.string.dashboard_greeting_afternoon)
            else -> resources.getString(R.string.dashboard_greeting_evening)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DashboardViewModel(
                    application = checkNotNull(this[AndroidViewModelFactory.APPLICATION_KEY]),
                    scheduleRepository = container.scheduleRepository,
                    taskRepository = container.taskRepository,
                    studySessionRepository = container.studySessionRepository,
                    settingRepository = container.settingRepository,
                    habitRepository = container.habitRepository
                )
            }
        }
    }
}
