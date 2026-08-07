package com.mother.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.mother.app.ui.components.NeoCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.R
import com.mother.app.data.local.entity.ScheduleEntity
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.local.entity.TaskEntity
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.data.repository.TaskRepository
import com.mother.app.di.AppContainer
import com.mother.app.ui.screens.statusLabel
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    /** The month currently displayed (any day within it). */
    val displayedMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val selectedDay: LocalDate = LocalDate.now(),
    /** Days with at least one activity, keyed by local date. */
    val activeDays: Set<LocalDate> = emptySet(),
    val daySchedules: List<ScheduleEntity> = emptyList(),
    val dayTasks: List<TaskEntity> = emptyList(),
    val daySessions: List<StudySessionEntity> = emptyList()
)

/** Month calendar with activity indicators and per-day drill-down (PRD §22). */
class CalendarViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val taskRepository: TaskRepository,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var schedules: List<ScheduleEntity> = emptyList()
    private var tasks: List<TaskEntity> = emptyList()
    private var sessions: List<StudySessionEntity> = emptyList()

    init {
        viewModelScope.launch {
            scheduleRepository.observeAll().collect { schedules = it; rebuild() }
        }
        viewModelScope.launch {
            taskRepository.observeAll().collect { tasks = it; rebuild() }
        }
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { sessions = it; rebuild() }
        }
    }

    fun previousMonth() = shiftMonth(-1)

    fun nextMonth() = shiftMonth(1)

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update { it.copy(displayedMonth = today.withDayOfMonth(1), selectedDay = today) }
        rebuild()
    }

    fun selectDay(day: LocalDate) {
        _uiState.update { it.copy(selectedDay = day) }
        rebuild()
    }

    private fun shiftMonth(delta: Int) {
        _uiState.update { it.copy(displayedMonth = it.displayedMonth.plusMonths(delta.toLong())) }
        rebuild()
    }

    private fun rebuild() {
        val state = _uiState.value
        val activeDays = mutableSetOf<LocalDate>()
        schedules.forEach { activeDays.add(TimeUtils.toLocalDate(it.startTime)) }
        tasks.forEach { task -> task.deadline?.let { activeDays.add(TimeUtils.toLocalDate(it)) } }
        sessions.forEach { activeDays.add(TimeUtils.toLocalDate(it.startTime)) }

        // Local start/end of the selected day.
        val selectedStart = state.selectedDay.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val selectedEnd = state.selectedDay.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val daySchedules = schedules.filter { it.startTime in selectedStart until selectedEnd }
            .sortedBy { it.startTime }
        val dayTasks = tasks.filter { task ->
            task.deadline != null && task.deadline in selectedStart until selectedEnd
        }
        val daySessions = sessions.filter { it.startTime in selectedStart until selectedEnd }
            .sortedBy { it.startTime }

        _uiState.update {
            it.copy(
                activeDays = activeDays,
                daySchedules = daySchedules,
                dayTasks = dayTasks,
                daySessions = daySessions
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CalendarViewModel(
                    container.scheduleRepository,
                    container.taskRepository,
                    container.studySessionRepository
                )
            }
        }
    }
}

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MonthHeader(
                month = state.displayedMonth,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
                onToday = viewModel::goToToday
            )
        }
        item {
            MonthGrid(
                month = state.displayedMonth,
                selectedDay = state.selectedDay,
                activeDays = state.activeDays,
                onSelectDay = viewModel::selectDay
            )
        }
        item {
            Text(
                text = TimeUtils.formatFullDate(
                    state.selectedDay.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(state.daySchedules, key = { "s_" + it.id }) { schedule ->
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        "${TimeUtils.formatTime(schedule.startTime)} - ${TimeUtils.formatTime(schedule.endTime)} " +
                            "(${statusLabel(schedule.status)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        items(state.dayTasks, key = { "t_" + it.id }) { task ->
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        stringResource(R.string.field_deadline) + ": " +
                            TimeUtils.formatTime(task.deadline ?: 0L),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        items(state.daySessions, key = { "ss_" + it.id }) { session ->
            NeoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.calendar_session), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${TimeUtils.formatTime(session.startTime)} - ${TimeUtils.formatTime(session.endTime)} " +
                            "(${TimeUtils.formatDurationCompact(session.durationMinute)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (state.daySchedules.isEmpty() && state.dayTasks.isEmpty() && state.daySessions.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.calendar_day_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    month: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.calendar_prev_month))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("id", "ID"))),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onToday) {
                Text(stringResource(R.string.calendar_today))
            }
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.calendar_next_month))
        }
    }
}

@Composable
private fun MonthGrid(
    month: LocalDate,
    selectedDay: LocalDate,
    activeDays: Set<LocalDate>,
    onSelectDay: (LocalDate) -> Unit
) {
    val firstDay = month.withDayOfMonth(1)
    // Monday-first grid: offset of the 1st from Monday.
    val leading = (firstDay.dayOfWeek.value - 1).coerceAtLeast(0)
    val daysInMonth = month.lengthOfMonth()
    val totalCells = ((leading + daysInMonth + 6) / 7) * 7
    val today = LocalDate.now()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        for (row in 0 until totalCells / 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leading + 1
                    val cellModifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                    if (dayNumber in 1..daysInMonth) {
                        val day = firstDay.plusDays((dayNumber - 1).toLong())
                        val isSelected = day == selectedDay
                        val isToday = day == today
                        val hasActivity = day in activeDays
                        Column(
                            modifier = cellModifier.clickable { onSelectDay(day) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (hasActivity) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .height(4.dp)
                                        .fillMaxWidth(0.35f)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    } else {
                        Box(modifier = cellModifier)
                    }
                }
            }
        }
    }
}
