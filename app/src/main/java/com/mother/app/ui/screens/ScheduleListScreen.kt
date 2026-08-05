package com.mother.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.mother.app.data.repository.ScheduleRepository
import com.mother.app.di.AppContainer
import com.mother.app.ui.components.EmptyState
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleListViewModel(private val scheduleRepository: ScheduleRepository) : ViewModel() {

    private val _schedules = MutableStateFlow<List<ScheduleEntity>>(emptyList())
    val schedules: StateFlow<List<ScheduleEntity>> = _schedules.asStateFlow()

    init {
        viewModelScope.launch {
            scheduleRepository.observeAll().collect { list ->
                _schedules.update { list }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ScheduleListViewModel(container.scheduleRepository) }
        }
    }
}

@Composable
fun ScheduleListScreen(viewModel: ScheduleListViewModel) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()

    if (schedules.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.CalendarMonth,
            title = stringResource(R.string.schedules_empty_title),
            description = stringResource(R.string.schedules_empty_description)
        )
        return
    }

    // Group schedules by local day, keeping chronological order.
    val grouped = schedules.groupBy { TimeUtils.startOfDay(it.startTime) }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (dayStart, daySchedules) ->
            item(key = "header_$dayStart") {
                Text(
                    text = TimeUtils.formatFullDate(dayStart),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(daySchedules, key = { it.id }) { schedule ->
                ScheduleRow(schedule = schedule)
            }
        }
    }
}

@Composable
private fun ScheduleRow(schedule: ScheduleEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(text = schedule.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${TimeUtils.formatTime(schedule.startTime)} - ${TimeUtils.formatTime(schedule.endTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = statusLabel(schedule.status),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
