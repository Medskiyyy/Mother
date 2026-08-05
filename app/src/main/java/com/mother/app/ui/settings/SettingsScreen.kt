package com.mother.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mother.app.MotherApplication
import com.mother.app.R
import com.mother.app.data.backup.BackupManager
import com.mother.app.data.local.entity.AppSettingEntity
import com.mother.app.data.model.Theme
import com.mother.app.data.repository.SettingRepository
import com.mother.app.di.AppContainer
import com.mother.app.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val setting: AppSettingEntity? = null,
    val message: String? = null,
    val busy: Boolean = false
)

class SettingsViewModel(
    private val container: AppContainer,
    private val settingRepository: SettingRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingRepository.observeSetting().collect { setting ->
                _uiState.update { it.copy(setting = setting) }
            }
        }
    }

    fun onThemeChange(theme: Theme) {
        viewModelScope.launch { settingRepository.updateTheme(theme.name) }
    }

    fun export(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            try {
                backupManager.export(uri)
                settingRepository.updateLastBackup(System.currentTimeMillis())
                _uiState.update { it.copy(busy = false, message = "export_ok") }
            } catch (e: Exception) {
                _uiState.update { it.copy(busy = false, message = e.message) }
            }
        }
    }

    /** Replaces the database from the picked file and restarts the app (UI_SPEC). */
    fun restore(uri: android.net.Uri, onRestored: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true) }
            try {
                backupManager.restore(uri)
                onRestored()
            } catch (e: Exception) {
                _uiState.update { it.copy(busy = false, message = e.message) }
            }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(message = null) }

    companion object {
        fun factory(container: AppContainer, backupManager: BackupManager): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingsViewModel(container, container.settingRepository, backupManager)
                }
            }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri -> uri?.let { viewModel.export(it) } }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    state.message?.let { message ->
        LaunchedEffect(message) {
            val text = when (message) {
                "export_ok" -> context.getString(R.string.backup_export_success)
                "restore_ok" -> context.getString(R.string.backup_restore_success)
                else -> message
            }
            snackbarHostState.showSnackbar(text)
            viewModel.dismissMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeCard(
                selected = state.setting?.theme ?: Theme.SYSTEM,
                onSelected = viewModel::onThemeChange
            )
            BackupCard(
                lastBackup = state.setting?.lastBackup,
                busy = state.busy,
                onExport = {
                    val fileName = "mother-backup-${
                        java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }.db"
                    exportLauncher.launch(fileName)
                },
                onImport = { restoreLauncher.launch(arrayOf("*/*")) }
            )
            AboutCard()
        }
    }

    // Restore confirmation (PRD §26): whole-database replacement is destructive.
    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text(stringResource(R.string.backup_restore_title)) },
            text = { Text(stringResource(R.string.backup_restore_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    val uri = pendingRestoreUri ?: return@TextButton
                    pendingRestoreUri = null
                    viewModel.restore(uri) {
                        // Restart the process so the restored database is used.
                        context.packageManager
                            .getLaunchIntentForPackage(context.packageName)
                            ?.let { intent ->
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                            }
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                }) {
                    Text(stringResource(R.string.backup_restore_action))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirm = false
                    pendingRestoreUri = null
                }) {
                    Text(stringResource(R.string.form_cancel))
                }
            }
        )
    }
}

@Composable
private fun ThemeCard(selected: Theme, onSelected: (Theme) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleMedium)
            Theme.entries.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(theme) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(selected = selected == theme, onClick = { onSelected(theme) })
                    Text(
                        text = stringResource(
                            when (theme) {
                                Theme.LIGHT -> R.string.theme_light
                                Theme.DARK -> R.string.theme_dark
                                Theme.SYSTEM -> R.string.theme_system
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupCard(lastBackup: Long?, busy: Boolean, onExport: () -> Unit, onImport: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.settings_backup), style = MaterialTheme.typography.titleMedium)
            Text(
                text = lastBackup?.let {
                    stringResource(R.string.backup_last, TimeUtils.formatFullDate(it))
                } ?: stringResource(R.string.backup_last_never),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onExport, enabled = !busy) {
                    Text(stringResource(R.string.backup_export))
                }
                TextButton(onClick = onImport, enabled = !busy) {
                    Text(stringResource(R.string.backup_import))
                }
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.about_version, versionName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
