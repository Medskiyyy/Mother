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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mother.app.R
import com.mother.app.ui.components.NeoButton
import com.mother.app.ui.components.NeoCard
import com.mother.app.ui.components.NeoOutlinedButton

private const val LAST_PAGE = 3

/**
 * First-run onboarding: 4 pages (UI_SPEC §Onboarding) — what Mother is, the
 * main features, how reminders work, and permissions. Shown once. Content
 * sits in a neobrutalist card with high-contrast text.
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

    val (titleRes, bodyRes) = when (page) {
        0 -> R.string.onboarding_title_1 to R.string.onboarding_body_1
        1 -> R.string.onboarding_title_2 to R.string.onboarding_body_2
        2 -> R.string.onboarding_title_3 to R.string.onboarding_body_3
        else -> R.string.onboarding_title_4 to R.string.onboarding_body_4
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                // Page 4: let the user grant the notification permission (PRD §27).
                if (page == LAST_PAGE && !notificationGranted) {
                    Spacer(Modifier.height(16.dp))
                    NeoOutlinedButton(
                        text = stringResource(R.string.onboarding_grant),
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        PageIndicator(page = page)
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeoOutlinedButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = onFinished,
                modifier = Modifier.weight(1f)
            )
            NeoButton(
                text = stringResource(if (page == LAST_PAGE) R.string.onboarding_start else R.string.onboarding_next),
                onClick = { if (page == LAST_PAGE) onFinished() else page++ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Neobrutalist page dots: the current page is a filled bordered square. */
@Composable
private fun PageIndicator(page: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(LAST_PAGE + 1) { index ->
            val active = index == page
            androidx.compose.material3.Surface(
                modifier = Modifier.size(if (active) 14.dp else 10.dp),
                shape = CircleShape,
                color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
            ) {}
        }
    }
}
