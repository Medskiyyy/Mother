package com.mother.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mother.app.data.timer.ActiveTimerStore
import com.mother.app.data.timer.TimerPhase
import kotlinx.coroutines.delay

/**
 * Bottom banner displaying the currently active timer.
 * Appears across top-level screens when a timer is running or paused.
 */
@Composable
fun ActiveTimerBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTimer by ActiveTimerStore.activeTimer.collectAsStateWithLifecycle()
    val timer = activeTimer

    val isVisible = timer != null && timer.phase != TimerPhase.IDLE

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (timer != null) {
            var elapsedSec by remember(timer.phase, timer.segmentStart) { mutableLongStateOf(0L) }

            LaunchedEffect(timer.phase, timer.segmentStart) {
                while (timer.phase == TimerPhase.RUNNING) {
                    elapsedSec = ActiveTimerStore.elapsedMillis() / 1000L
                    delay(1000L)
                }
                elapsedSec = ActiveTimerStore.elapsedMillis() / 1000L
            }

            val hours = elapsedSec / 3600L
            val minutes = (elapsedSec % 3600L) / 60L
            val seconds = elapsedSec % 60L
            val timeDisplay = if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }

            NeoCard(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color(0xFF121212),
                borderColor = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (timer.phase == TimerPhase.PAUSED) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color(0xFF121212),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = timer.habitTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF121212),
                                maxLines = 1
                            )
                            Text(
                                text = if (timer.phase == TimerPhase.PAUSED) "Timer Dijeda • $timeDisplay" else "Mode Fokus • $timeDisplay",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF121212).copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Buka Focus Mode",
                        tint = Color(0xFF121212),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
