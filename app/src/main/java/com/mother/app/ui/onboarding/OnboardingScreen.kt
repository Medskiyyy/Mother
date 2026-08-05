package com.mother.app.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mother.app.R

private const val LAST_PAGE = 3

/**
 * First-run onboarding: 4 pages (UI_SPEC §Onboarding) — what Mother is, the
 * main features, how reminders work, and permissions. Shown once.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Outcome does not change the flow; the app works either way.
        }
    val notificationGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (titleRes, bodyRes) = when (page) {
            0 -> R.string.onboarding_title_1 to R.string.onboarding_body_1
            1 -> R.string.onboarding_title_2 to R.string.onboarding_body_2
            2 -> R.string.onboarding_title_3 to R.string.onboarding_body_3
            else -> R.string.onboarding_title_4 to R.string.onboarding_body_4
        }
        Text(text = stringResource(titleRes), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Page 4: let the user grant the notification permission (PRD §27).
        if (page == LAST_PAGE && !notificationGranted) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            ) {
                Text(stringResource(R.string.onboarding_grant))
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onFinished) {
                Text(stringResource(R.string.onboarding_skip))
            }
            if (page == LAST_PAGE) {
                Button(onClick = onFinished) {
                    Text(stringResource(R.string.onboarding_start))
                }
            } else {
                Button(onClick = { page++ }) {
                    Text(stringResource(R.string.onboarding_next))
                }
            }
        }
    }
}
