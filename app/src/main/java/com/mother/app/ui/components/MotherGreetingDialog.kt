package com.mother.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mother.app.ui.theme.NeoStreakYellow
import kotlin.random.Random

/**
 * Smart Contextual Message Generator:
 * Generates dynamic motherly messages based on time of day, streak count, and task/schedule counts.
 */
fun generateMotherMessage(
    taskCount: Int,
    scheduleCount: Int,
    streak: Int,
    urgentTaskTitle: String? = null
): String {
    val hour = java.time.LocalTime.now().hour
    val timeGreeting = when (hour) {
        in 5..11 -> "Semangat pagi"
        in 12..15 -> "Selamat siang"
        in 16..18 -> "Selamat sore"
        else -> "Selamat malam"
    }

    return when {
        // Urgent task exists
        !urgentTaskTitle.isNullOrBlank() -> {
            "$timeGreeting! Hari ini kamu punya tugas mendesak \"$urgentTaskTitle\". Selesaikan yang ini dulu ya, biar pikiranmu tenang."
        }
        // High streak
        streak >= 3 -> {
            "Wah, hebat sekali! Streak belajarmu sudah $streak hari berturut-turut. Pertahankan konsistensimu hari ini ya."
        }
        // Heavy day
        taskCount + scheduleCount >= 4 -> {
            "$timeGreeting! Agenda dan tugasmu hari ini cukup padat ($taskCount tugas & $scheduleCount jadwal). Kerjakan satu per satu dengan tenang ya."
        }
        // Light day
        taskCount == 0 && scheduleCount == 0 -> {
            "$timeGreeting! Hari ini agenda dan tugasmu masih kosong. Gunakan waktu luang ini untuk istirahat atau mengulang materi santai ya."
        }
        // Night time
        hour >= 21 -> {
            "Sudah malam. Jangan lupa istirahat yang cukup ya, kesehatanmu jauh lebih berharga daripada porsi belajar berlebih."
        }
        // Default contextual greetings
        else -> {
            val templates = listOf(
                "$timeGreeting! Mari selesaikan aktivitas dan tugasmu satu per satu hari ini.",
                "Fokus dan konsisten hari ini ya! Setiap langkah kecil yang kamu ambil sangat berarti.",
                "Jaga kesehatan dan jangan lupa minum air putih di sela-sela belajar ya.",
                "Kalau merasa lelah, tarik napas dalam-dalam dan istirahat sejenak. Kamu sudah berusaha baik."
            )
            templates.random()
        }
    }
}

@Composable
fun MotherGreetingDialog(
    taskCount: Int,
    scheduleCount: Int,
    streak: Int = 0,
    urgentTaskTitle: String? = null,
    onDismiss: () -> Unit
) {
    val dynamicMessage = remember(taskCount, scheduleCount, streak, urgentTaskTitle) {
        generateMotherMessage(taskCount, scheduleCount, streak, urgentTaskTitle)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            NeoButton(
                text = "Siap, Bu!",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeoStreakYellow)
                        .border(2.dp, Color(0xFF121212), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFF121212),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pesan dari Ibu ❤️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pengingat Kasih Sayang",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = dynamicMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "RINGKASAN HARI INI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "• $taskCount Deadline / Tugas menunggu",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• $scheduleCount Aktivitas jadwal hari ini",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )
}
