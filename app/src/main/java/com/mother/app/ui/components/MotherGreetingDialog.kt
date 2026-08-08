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

private val MOTHER_MESSAGES = listOf(
    "Semangat pagi anakku! Ibu percaya kamu pasti bisa melewati dan menyelesaikan target hari ini dengan baik.",
    "Jangan lupa istirahat ya nak. Belajar itu penting, tapi kesehatanmu jauh lebih berharga untuk Ibu.",
    "Halo sayang! Hari baru, kesempatan baru. Mari selesaikan aktivitas dan tugasmu satu per satu ya.",
    "Bismillah, fokus dan konsisten hari ini! Setiap langkah kecil yang kamu ambil sangat membanggakan Ibu.",
    "Jaga kesehatan dan jangan lupa minum air putih ya! Ibu selalu mendukung setiap perjuanganmu hari ini."
)

@Composable
fun MotherGreetingDialog(
    taskCount: Int,
    scheduleCount: Int,
    onDismiss: () -> Unit
) {
    val randomMessage = remember { MOTHER_MESSAGES[Random.nextInt(MOTHER_MESSAGES.size)] }

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
                    text = "\"$randomMessage\"",
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
