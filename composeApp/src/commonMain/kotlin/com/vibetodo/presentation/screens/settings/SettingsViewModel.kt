package com.vibetodo.presentation.screens.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.data.sync.SyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dbHelper: DatabaseHelper,
    private val syncService: SyncService,
) : ScreenModel {

    private val _passphrase = MutableStateFlow("")
    val passphrase: StateFlow<String> = _passphrase.asStateFlow()

    private val _dropboxClientId = MutableStateFlow("")
    val dropboxClientId: StateFlow<String> = _dropboxClientId.asStateFlow()

    private val _dropboxToken = MutableStateFlow("")
    val dropboxToken: StateFlow<String> = _dropboxToken.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _backupPath = MutableStateFlow("")
    val backupPath: StateFlow<String> = _backupPath.asStateFlow()

    init {
        screenModelScope.launch {
            _passphrase.value = dbHelper.getConfig("encryption_passphrase") ?: ""
            _dropboxClientId.value = dbHelper.getConfig("dropbox_client_id") ?: ""
            _dropboxToken.value = dbHelper.getConfig("dropbox_token") ?: ""
            _lastSyncTime.value = dbHelper.getConfig("last_sync_time")
            _backupPath.value = dbHelper.getConfig("backup_path") ?: "${System.getProperty("user.home")}/.qululist/backup.enc"
        }
    }

    fun updatePassphrase(value: String) {
        _passphrase.value = value
        screenModelScope.launch { dbHelper.setConfig("encryption_passphrase", value) }
    }

    fun updateDropboxClientId(value: String) {
        _dropboxClientId.value = value
        screenModelScope.launch { dbHelper.setConfig("dropbox_client_id", value) }
    }

    fun updateDropboxToken(value: String) {
        _dropboxToken.value = value
        screenModelScope.launch { dbHelper.setConfig("dropbox_token", value) }
    }

    fun getDropboxAuthUrl(): String {
        return syncService.getDropboxAuthUrl(_dropboxClientId.value)
    }

    fun exportBackup() {
        if (_passphrase.value.isBlank()) {
            _statusMessage.value = "Set a passphrase first"
            return
        }
        _isSyncing.value = true
        screenModelScope.launch {
            val success = syncService.exportEncryptedBackup(_passphrase.value, _backupPath.value)
            _statusMessage.value = if (success) "Backup exported to ${_backupPath.value}" else "Export failed"
            _isSyncing.value = false
        }
    }

    fun importBackup() {
        if (_passphrase.value.isBlank()) {
            _statusMessage.value = "Set a passphrase first"
            return
        }
        _isSyncing.value = true
        screenModelScope.launch {
            val success = syncService.importEncryptedBackup(_backupPath.value, _passphrase.value)
            _statusMessage.value = if (success) "Backup imported successfully" else "Import failed (wrong passphrase or corrupt file)"
            _isSyncing.value = false
        }
    }

    fun syncToDropbox() {
        if (_passphrase.value.isBlank()) { _statusMessage.value = "Set a passphrase first"; return }
        if (_dropboxToken.value.isBlank()) { _statusMessage.value = "Connect Dropbox first"; return }
        _isSyncing.value = true
        screenModelScope.launch {
            val success = syncService.syncToDropbox(_dropboxToken.value, _passphrase.value)
            if (success) {
                _lastSyncTime.value = kotlinx.datetime.Clock.System.now().toString()
                dbHelper.setConfig("last_sync_time", _lastSyncTime.value!!)
            }
            _statusMessage.value = if (success) "Uploaded to Dropbox" else "Dropbox upload failed"
            _isSyncing.value = false
        }
    }

    fun syncFromDropbox() {
        if (_passphrase.value.isBlank()) { _statusMessage.value = "Set a passphrase first"; return }
        if (_dropboxToken.value.isBlank()) { _statusMessage.value = "Connect Dropbox first"; return }
        _isSyncing.value = true
        screenModelScope.launch {
            val success = syncService.syncFromDropbox(_dropboxToken.value, _passphrase.value)
            _statusMessage.value = if (success) "Restored from Dropbox" else "Dropbox download failed (wrong passphrase or no backup)"
            _isSyncing.value = false
        }
    }

    fun clearStatus() { _statusMessage.value = "" }
}
