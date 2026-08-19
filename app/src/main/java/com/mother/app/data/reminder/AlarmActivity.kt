package com.mother.app.data.reminder

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mother.app.MainActivity
import com.mother.app.ui.components.NeoButton
import com.mother.app.ui.components.NeoOutlinedButton
import com.mother.app.ui.components.neoShadow
import com.mother.app.ui.theme.Ink
import com.mother.app.ui.theme.InkSoft
import com.mother.app.ui.theme.MotherTheme
import com.mother.app.ui.theme.NeoStreakYellow
import com.mother.app.ui.theme.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Full-screen alarm UI for habit reminders.
 * Clean full-screen warm gradient background without dark card wrapper.
 * Sound and vibration are managed exclusively by [HabitAlarmService].
 */
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen & show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        @Suppress("DEPRECATION")
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val ownerType = intent.getStringExtra(ReminderScheduler.EXTRA_OWNER_TYPE) ?: ""
        val ownerId = intent.getStringExtra(ReminderScheduler.EXTRA_OWNER_ID) ?: ""
        val reminderId = intent.getStringExtra(ReminderScheduler.EXTRA_REMINDER_ID) ?: ""
        val title = intent.getStringExtra("title") ?: "Waktunya Kebiasaan!"

        setContent {
            MotherTheme {
                AlarmScreen(
                    title = title,
                    onStart = {
                        HabitAlarmService.stop(this)
                        dismissReminder(reminderId)
                        val openIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(openIntent)
                        finish()
                    },
                    onSnooze = {
                        HabitAlarmService.stop(this)
                        dismissReminder(reminderId)
                        snoozeReminder(ownerType, ownerId, reminderId)
                        finish()
                    },
                    onSkip = {
                        HabitAlarmService.stop(this)
                        dismissReminder(reminderId)
                        finish()
                    }
                )
            }
        }
    }

    private fun dismissReminder(reminderId: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.cancel(ReminderScheduler.requestCode(reminderId))
        nm.cancel(ReminderScheduler.requestCode("missed_$reminderId"))
        nm.cancel(HabitAlarmService.ALARM_NOTIFICATION_ID)
    }

    private fun snoozeReminder(ownerType: String, ownerId: String, reminderId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val newTrigger = System.currentTimeMillis() + 5 * 60_000L
            ReminderScheduler.schedule(applicationContext, ownerType, ownerId, reminderId, newTrigger)
        }
    }

    override fun onDestroy() {
        HabitAlarmService.stop(this)
        super.onDestroy()
    }
}

@Composable
private fun AlarmScreen(
    title: String,
    onStart: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    // Warm gradient background from cream to soft golden sand
    val warmGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFDF7),
            Color(0xFFF6ECE1),
            Color(0xFFEBDAC4)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(warmGradient)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Alarm Icon with solid Neobrutalism shadow
            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp, end = 4.dp)
                    .neoShadow(color = Ink, shape = CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(NeoStreakYellow)
                        .border(3.5.dp, Ink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Category Badge & Mother Tag
            Text(
                text = "ALARM KEBIASAAN • PESAN DARI IBU 💛",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = InkSoft,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(10.dp))

            // Habit Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    lineHeight = 34.sp
                ),
                color = Ink,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Dynamic Mother-like Encouragement Message (Pesan dari Ibu)
            val motherMessage = androidx.compose.runtime.remember {
                listOf(
                    "Ayo anak pintar, disiplin itu kunci suksesmu. Luangkan waktu sejenak untuk kebiasaan ini ya!",
                    "Ibu percaya kamu pasti bisa konsisten hari ini. Jangan ditunda-tunda ya, Nak!",
                    "Langkah kecil setiap hari akan membawa perubahan besar. Yuk selesaikan kebiasaan baikmu!",
                    "Ayo luangkan waktu sebentar. Istirahat boleh, tapi kebiasaan baikmu jangan dilupakan ya!",
                    "Setiap kali kamu menyelesaikan kebiasaan ini, kamu makin dekat dengan cita-citamu. Semangat!",
                    "Jaga konsistensimu hari ini ya! Ibu selalu bangga kalau melihatmu disiplin setiap hari.",
                    "Sudah waktunya! Tarik napas sebentar, fokus, dan selesaikan kebiasaan baik ini sekarang."
                ).random()
            }

            Text(
                text = "\"$motherMessage\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(44.dp))

            // Main Action Button: Mulai Kebiasaan (Larger & Thicker)
            NeoButton(
                text = "Mulai Kebiasaan Sekarang",
                onClick = onStart,
                containerColor = NeoStreakYellow,
                contentColor = Ink,
                borderColor = Ink,
                fontSize = 17.sp,
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // Secondary Buttons: Tunda 5 Mnt & Lewati (Equal size, Single-line text, No wrapping)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeoOutlinedButton(
                    text = "Tunda 5 Mnt",
                    onClick = onSnooze,
                    containerColor = Ink,
                    contentColor = Color.White,
                    borderColor = Ink,
                    fontSize = 14.sp,
                    contentPadding = PaddingValues(vertical = 15.dp, horizontal = 2.dp),
                    modifier = Modifier.weight(1f)
                )
                NeoOutlinedButton(
                    text = "Lewati",
                    onClick = onSkip,
                    containerColor = Ink,
                    contentColor = Color.White,
                    borderColor = Ink,
                    fontSize = 14.sp,
                    contentPadding = PaddingValues(vertical = 15.dp, horizontal = 2.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
