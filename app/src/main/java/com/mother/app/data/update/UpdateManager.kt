package com.mother.app.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.mother.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(
    private val context: Context,
    private val repoOwner: String = "Medskiyyy",
    private val repoName: String = "Mother"
) {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    /**
     * Checks GitHub releases for a newer version than [currentVersion].
     */
    suspend fun checkForUpdates(
        currentVersion: String = BuildConfig.VERSION_NAME,
        isManualCheck: Boolean = false
    ): AppReleaseInfo? = withContext(Dispatchers.IO) {
        if (isManualCheck) {
            _updateState.value = UpdateState.Checking
        }
        try {
            val endpoint = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val jsonString = fetchUrlString(endpoint) ?: run {
                if (isManualCheck) {
                    _updateState.value = UpdateState.Error("Network error")
                }
                return@withContext null
            }

            val release = parseGitHubReleaseJson(jsonString)
            if (release != null && isNewerVersion(currentVersion, release.versionName)) {
                _updateState.value = UpdateState.UpdateAvailable(release)
                release
            } else {
                if (isManualCheck) {
                    _updateState.value = UpdateState.UpToDate
                }
                null
            }
        } catch (e: Exception) {
            if (isManualCheck) {
                _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
            }
            null
        }
    }

    /**
     * Downloads the APK file from GitHub release with download progress updates.
     */
    suspend fun downloadUpdate(release: AppReleaseInfo): File? = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Downloading(release, 0)
        try {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updatesDir, release.apkFileName)

            var connection = (URL(release.apkDownloadUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mother-App-Updater")
            }

            var redirectCount = 0
            while (connection.responseCode in listOf(HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_SEE_OTHER, 307, 308) && redirectCount < 5) {
                val newUrl = connection.getHeaderField("Location") ?: break
                connection.disconnect()
                connection = (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mother-App-Updater")
                }
                redirectCount++
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                _updateState.value = UpdateState.Error("HTTP ${connection.responseCode}")
                return@withContext null
            }

            val contentLength = connection.contentLength
            var totalRead = 0L

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val percent = ((totalRead * 100) / contentLength).toInt().coerceIn(0, 100)
                            _updateState.value = UpdateState.Downloading(release, percent)
                        }
                    }
                    output.flush()
                }
            }

            _updateState.value = UpdateState.ReadyToInstall(release, apkFile)
            apkFile
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Download failed")
            null
        }
    }

    /**
     * Prompts the Android package installer to install the downloaded APK.
     */
    fun installUpdate(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Failed to launch installer")
        }
    }

    fun dismiss() {
        _updateState.value = UpdateState.Idle
    }

    private fun fetchUrlString(urlString: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 10000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Mother-App-Updater")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    companion object {
        /**
         * Parses GitHub release JSON string into [AppReleaseInfo].
         */
        fun parseGitHubReleaseJson(jsonString: String): AppReleaseInfo? {
            return try {
                fun extractStringField(key: String, source: String): String {
                    val pattern = "\"$key\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"".toRegex()
                    val match = pattern.find(source) ?: return ""
                    val raw = match.groupValues[1]
                    return raw.replace("\\n", "\n")
                        .replace("\\r", "")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                }

                val tagName = extractStringField("tag_name", jsonString).removePrefix("v").removePrefix("V").trim()
                if (tagName.isBlank()) return null

                val name = extractStringField("name", jsonString).ifBlank { "Mother v$tagName" }
                val body = extractStringField("body", jsonString)
                val htmlUrl = extractStringField("html_url", jsonString)

                var downloadUrl = ""
                var apkName = "Mother-v$tagName.apk"

                // Find asset with .apk in name
                val assetPattern = "\\{[^{}]*\"name\"\\s*:\\s*\"([^\"]+\\.apk)\"[^{}]*\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}".toRegex(RegexOption.IGNORE_CASE)
                val assetMatch = assetPattern.find(jsonString)
                if (assetMatch != null) {
                    apkName = assetMatch.groupValues[1]
                    downloadUrl = assetMatch.groupValues[2]
                } else {
                    // Try alternative key order: browser_download_url then name
                    val assetPattern2 = "\\{[^{}]*\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"[^{}]*\"name\"\\s*:\\s*\"([^\"]+\\.apk)\"[^{}]*\\}".toRegex(RegexOption.IGNORE_CASE)
                    val assetMatch2 = assetPattern2.find(jsonString)
                    if (assetMatch2 != null) {
                        downloadUrl = assetMatch2.groupValues[1]
                        apkName = assetMatch2.groupValues[2]
                    } else {
                        // Any browser_download_url
                        val anyUrlPattern = "\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        val anyMatch = anyUrlPattern.find(jsonString)
                        if (anyMatch != null) {
                            downloadUrl = anyMatch.groupValues[1]
                        }
                    }
                }

                if (downloadUrl.isEmpty()) {
                    downloadUrl = htmlUrl
                }

                AppReleaseInfo(
                    versionName = tagName,
                    releaseTitle = name,
                    releaseNotes = body,
                    apkDownloadUrl = downloadUrl,
                    apkFileName = apkName,
                    htmlUrl = htmlUrl
                )
            } catch (_: Exception) {
                null
            }
        }

        /**
         * Returns true if [latestVersion] is strictly newer than [currentVersion].
         * E.g. "3.6.0" is newer than "3.5.0", "3.5.1" is newer than "3.5.0".
         */
        fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
            val cleanCurrent = currentVersion.trim().removePrefix("v").removePrefix("V")
            val cleanLatest = latestVersion.trim().removePrefix("v").removePrefix("V")

            if (cleanCurrent == cleanLatest) return false

            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
            val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

            val maxParts = maxOf(currentParts.size, latestParts.size)
            for (i in 0 until maxParts) {
                val c = currentParts.getOrElse(i) { 0 }
                val l = latestParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            return false
        }
    }
}
