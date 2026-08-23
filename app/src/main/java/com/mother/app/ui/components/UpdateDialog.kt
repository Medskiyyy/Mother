package com.mother.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mother.app.BuildConfig
import com.mother.app.R
import com.mother.app.data.update.AppReleaseInfo
import com.mother.app.data.update.UpdateState
import com.mother.app.ui.theme.DarkColors
import com.mother.app.ui.theme.LightColors
import com.mother.app.ui.theme.isAppInDarkTheme
import java.io.File

/**
 * Neobrutalist In-App Update Dialog.
 * Perfectly calibrated for both Light and Dark themes.
 */
@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onStartDownload: (AppReleaseInfo) -> Unit,
    onInstall: (File) -> Unit,
    onDismiss: () -> Unit
) {
    if (updateState is UpdateState.Idle || updateState is UpdateState.UpToDate) {
        return
    }

    val isDark = isAppInDarkTheme()

    // Colors customized for Light and Dark themes
    val cardBg = if (isDark) DarkColors.Surface else LightColors.Surface
    val cardBorder = if (isDark) DarkColors.Outline else LightColors.Outline
    val textColor = if (isDark) DarkColors.TextPrimary else LightColors.TextPrimary
    val textSecondary = if (isDark) DarkColors.TextSecondary else LightColors.TextSecondary

    val headerBadgeBg = if (isDark) DarkColors.SolidYellow else LightColors.SolidYellow
    val headerBadgeText = Color(0xFF121212)
    val headerBadgeBorder = if (isDark) DarkColors.Outline else LightColors.Outline

    val changelogBg = if (isDark) Color(0xFF141418) else LightColors.Background
    val changelogBorder = if (isDark) Color(0xFF34343E) else LightColors.Outline

    val versionOldBg = if (isDark) DarkColors.SurfaceVariant else LightColors.SurfaceVariant
    val versionOldText = if (isDark) DarkColors.TextSecondary else LightColors.TextSecondary
    val versionNewBg = if (isDark) DarkColors.PriorityAmanBg else LightColors.PriorityAmanBg
    val versionNewText = if (isDark) DarkColors.PriorityAmanText else LightColors.PriorityAmanText
    val versionNewBorder = if (isDark) DarkColors.PriorityAmanBorder else LightColors.Outline

    val progressTrack = if (isDark) DarkColors.SurfaceVariant else LightColors.SurfaceVariant
    val progressFill = if (isDark) DarkColors.SolidYellow else LightColors.SolidYellow

    Dialog(
        onDismissRequest = {
            if (updateState !is UpdateState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(dismissOnClickOutside = updateState !is UpdateState.Downloading)
    ) {
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = cardBg,
            contentColor = textColor,
            borderColor = cardBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(headerBadgeBg)
                            .border(2.dp, headerBadgeBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when (updateState) {
                                    is UpdateState.ReadyToInstall -> Icons.Filled.DownloadDone
                                    is UpdateState.Downloading -> Icons.Filled.CloudDownload
                                    else -> Icons.Filled.NewReleases
                                },
                                contentDescription = null,
                                tint = headerBadgeText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.update_available_title),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = headerBadgeText,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                when (updateState) {
                    is UpdateState.UpdateAvailable -> {
                        val release = updateState.release
                        Text(
                            text = release.releaseTitle.ifBlank { "Mother v${release.versionName}" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = textColor
                        )

                        // Version Comparison Badges: current -> new
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(versionOldBg)
                                    .border(1.5.dp, cardBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = versionOldText
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(versionNewBg)
                                    .border(1.5.dp, versionNewBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "v${release.versionName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = versionNewText
                                )
                            }
                        }

                        // Changelog Box
                        if (release.releaseNotes.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(changelogBg)
                                    .border(2.dp, changelogBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.update_changelog_title),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = release.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor,
                                    modifier = Modifier
                                        .heightIn(max = 140.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                            }
                        }

                        // Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NeoButton(
                                text = stringResource(R.string.update_action_now),
                                onClick = { onStartDownload(release) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.update_action_later),
                                    fontWeight = FontWeight.Bold,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        val progress = updateState.progressPercent
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress / 100f,
                            label = "downloadProgress"
                        )

                        Text(
                            text = stringResource(R.string.update_downloading, progress),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = textColor
                        )

                        // Neobrutalist Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(progressTrack)
                                .border(2.dp, cardBorder, RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(20.dp)
                                    .background(progressFill)
                            )
                        }

                        Text(
                            text = "Harap tunggu, file APK sedang diunduh...",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                    }

                    is UpdateState.ReadyToInstall -> {
                        val file = updateState.apkFile
                        Text(
                            text = "Unduhan Selesai!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = textColor
                        )
                        Text(
                            text = "File pembaruan sudah siap dipasang ke perangkat Anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NeoButton(
                                text = stringResource(R.string.update_action_install),
                                onClick = { onInstall(file) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.form_cancel),
                                    fontWeight = FontWeight.Bold,
                                    color = textSecondary
                                )
                            }
                        }
                    }

                    is UpdateState.Error -> {
                        val message = updateState.message
                        Text(
                            text = "Gagal Memperbarui",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary
                        )
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = stringResource(R.string.form_cancel),
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}
