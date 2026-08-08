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
    "Semangat pagi! Kamu pasti bisa melewati dan menyelesaikan target hari ini dengan baik.",
    "Jangan lupa istirahat ya. Belajar itu penting, tapi kesehatanmu jauh lebih berharga.",
    "Hari baru, kesempatan baru. Mari selesaikan aktivitas dan tugasmu satu per satu ya.",
    "Bismillah, fokus dan konsisten hari ini! Setiap langkah kecil yang kamu ambil sangat berarti.",
    "Jaga kesehatan dan jangan lupa minum air putih ya! Tetap semangat menjalani aktivitas hari ini.",
    "Kalau merasa lelah, tarik napas dalam-dalam dan istirahat sejenak. Kamu sudah berusaha sangat baik.",
    "Kerjakan apa yang bisa kamu selesaikan hari ini tanpa perlu terburu-buru. Pelan tapi pasti!",
    "Fokus pada prosesnya ya. Ibu yakin kamu bisa mengatasi setiap kendala hari ini.",
    "Jangan lupa makan tepat waktu! Tubuh yang sehat bikin pikiranmu makin jernih.",
    "Setiap tantangan adalah kesempatan belajar. Percaya pada kemampuan dirimu sendiri ya.",
    "Awali harimu dengan senyuman dan niat yang baik. Semoga harimu menyenangkan dan lancar!",
    "Ingat untuk tidak menunda hal-hal kecil. Selesaikan sekarang supaya nanti malam bisa istirahat tenang.",
    "Kamu luar biasa sudah bertahan dan berjuang sejauh ini. Teruskan semangatmu hari ini!",
    "Belajar itu tentang konsistensi, bukan kecepatan. Ambil waktu yang kamu butuhkan.",
    "Semoga hari ini penuh dengan kemudahan dan hasil yang memuaskan untuk semua usahamu."
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
                    text = randomMessage,
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
