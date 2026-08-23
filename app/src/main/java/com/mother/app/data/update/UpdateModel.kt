package com.mother.app.data.update

import java.io.File

data class AppReleaseInfo(
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val htmlUrl: String
)

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class UpdateAvailable(val release: AppReleaseInfo) : UpdateState
    data class Downloading(val release: AppReleaseInfo, val progressPercent: Int) : UpdateState
    data class ReadyToInstall(val release: AppReleaseInfo, val apkFile: File) : UpdateState
    object UpToDate : UpdateState
    data class Error(val message: String) : UpdateState
}
