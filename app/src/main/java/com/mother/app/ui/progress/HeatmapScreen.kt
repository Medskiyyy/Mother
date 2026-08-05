package com.mother.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.R
import com.mother.app.data.local.entity.StudySessionEntity
import com.mother.app.data.repository.StudySessionRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/** Number of weeks shown in the heatmap grid. */
private const val HEATMAP_WEEKS = 16

data class HeatmapUiState(
    /** Minutes studied per day, keyed by local start-of-day. */
    val minutesByDay: Map<Long, Int> = emptyMap(),
    val totalActiveDays: Int = 0,
    val bestDay: Long? = null,
    val longestDurationMinutes: Int = 0
)

/** Consistency heatmap derived from StudySession rows (PRD §24). */
class HeatmapViewModel(
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studySessionRepository.observeAllAsc().collect { sessions ->
                rebuild(sessions)
            }
        }
    }

    private fun rebuild(sessions: List<StudySessionEntity>) {
        val minutesByDay = sessions.groupBy { TimeUtils.startOfDay(it.startTime) }
            .mapValues { (_, list) -> list.sumOf { it.durationMinute } }
        val activeEntries = minutesByDay.filter { it.value > 0 }
        val bestDay = activeEntries.maxByOrNull { it.value }?.key
        _uiState.update {
            HeatmapUiState(
                minutesByDay = minutesByDay,
                totalActiveDays = activeEntries.size,
                bestDay = bestDay,
                longestDurationMinutes = activeEntries.values.maxOrNull() ?: 0
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HeatmapViewModel(container.studySessionRepository) }
        }
    }
}

@Composable
fun HeatmapScreen(viewModel: HeatmapViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val today = TimeUtils.startOfDay(System.currentTimeMillis())
    // Monday of the earliest shown week.
    val firstWeekStart = TimeUtils.startOfWeek(TimeUtils.plusDays(today, -7L * (HEATMAP_WEEKS - 1)))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeatmapGrid(firstWeekStart = firstWeekStart, today = today, minutesByDay = state.minutesByDay)
        }
        item {
            HeatmapLegend()
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.heatmap_summary), style = MaterialTheme.typography.titleMedium)
                    SummaryRow(stringResource(R.string.heatmap_active_days), state.totalActiveDays.toString())
                    SummaryRow(
                        stringResource(R.string.heatmap_best_day),
                        state.bestDay?.let { TimeUtils.formatFullDate(it) }
                            ?: stringResource(R.string.field_not_selected)
                    )
                    SummaryRow(
                        stringResource(R.string.heatmap_longest),
                        TimeUtils.formatDurationCompact(state.longestDurationMinutes)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapGrid(firstWeekStart: Long, today: Long, minutesByDay: Map<Long, Int>) {
    // One column per week; each column stacks Monday..Sunday.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (week in 0 until HEATMAP_WEEKS) {
            val weekStart = TimeUtils.plusDays(firstWeekStart, 7L * week)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (dayOffset in 0 until 7) {
                    val day = TimeUtils.plusDays(weekStart, dayOffset.toLong())
                    val minutes = minutesByDay[day] ?: 0
                    val isFuture = day > today
                    val dayOfWeek = TimeUtils.toLocalDate(day).dayOfWeek
                    HeatmapCell(
                        minutes = minutes,
                        enabled = !isFuture && (day <= today || dayOfWeek <= DayOfWeek.SUNDAY)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(minutes: Int, enabled: Boolean) {
    // Intensity thresholds in minutes: none / light / medium / heavy / max.
    val color = when {
        !enabled || minutes <= 0 -> MaterialTheme.colorScheme.surfaceVariant
        minutes < 30 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        minutes < 60 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        minutes < 120 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}

@Composable
private fun HeatmapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            stringResource(R.string.heatmap_legend_less),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            listOf(0f, 0.3f, 0.55f, 0.8f, 1f).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (alpha == 0f) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                        )
                )
            }
        }
        Text(
            stringResource(R.string.heatmap_legend_more),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
