package com.mother.app.data.backup

import android.content.Context
import android.net.Uri
import com.mother.app.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manual, fully-offline backup & restore (PRD §26). The backup is a copy of
 * the Room database file; restore replaces the current database entirely and
 * the app restarts afterwards (UI_SPEC Import Flow). Attachments are not part
 * of v1 backups because file pickers return only the database stream.
 */
class BackupManager(private val context: Context, private val container: AppContainer) {

    /** Streams the database into [target]; returns bytes written. */
    suspend fun export(target: Uri): Long = withContext(Dispatchers.IO) {
        // Flush pending writes from the WAL into the main database file.
        container.checkpoint()
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        databaseFile.inputStream().use { input ->
            context.contentResolver.openOutputStream(target)?.use { output ->
                input.copyTo(output)
            } ?: error("Tidak dapat membuka tujuan backup.")
        }
        databaseFile.length()
    }

    /**
     * Replaces the database with [source]. The caller must confirm beforehand
     * (PRD §26); the app should restart after this returns.
     */
    suspend fun restore(source: Uri) = withContext(Dispatchers.IO) {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        container.closeForRestore()
        databaseFile.delete()
        File(databaseFile.path + "-wal").delete()
        File(databaseFile.path + "-shm").delete()
        context.contentResolver.openInputStream(source)?.use { input ->
            databaseFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Tidak dapat membaca file backup.")
    }

    companion object {
        const val DATABASE_NAME = "mother.db"
    }
}
