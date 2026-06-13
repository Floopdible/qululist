package com.vibetodo.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.data.sync.SyncService
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val dbHelper = koinInject<DatabaseHelper>()
        val syncService = koinInject<SyncService>()
        val vm = rememberScreenModel { SettingsViewModel(dbHelper, syncService) }

        val passphrase by vm.passphrase.collectAsState()
        val dropboxClientId by vm.dropboxClientId.collectAsState()
        val dropboxToken by vm.dropboxToken.collectAsState()
        val lastSyncTime by vm.lastSyncTime.collectAsState()
        val statusMessage by vm.statusMessage.collectAsState()
        val isSyncing by vm.isSyncing.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Encryption section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Encryption", style = MaterialTheme.typography.titleMedium)
                        Text("Used to encrypt your backup before syncing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = passphrase,
                            onValueChange = vm::updatePassphrase,
                            label = { Text("Passphrase") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { vm.exportBackup() }, enabled = !isSyncing) {
                                Text("Export Backup")
                            }
                            OutlinedButton(onClick = { vm.importBackup() }, enabled = !isSyncing) {
                                Text("Import Backup")
                            }
                        }
                    }
                }

                // Dropbox section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Dropbox Sync", style = MaterialTheme.typography.titleMedium)
                        Text("1. Create an app at dropbox.com/developers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("2. Enter your App key below", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = dropboxClientId,
                            onValueChange = vm::updateDropboxClientId,
                            label = { Text("Dropbox App Key") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (dropboxClientId.isNotBlank()) {
                            Text(
                                "3. Click the link below, authorize, then paste the code as the token",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = vm.getDropboxAuthUrl(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        OutlinedTextField(
                            value = dropboxToken,
                            onValueChange = vm::updateDropboxToken,
                            label = { Text("Access Token") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (dropboxToken.isNotBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { vm.syncToDropbox() }, enabled = !isSyncing) {
                                    Text("Upload to Dropbox")
                                }
                                OutlinedButton(onClick = { vm.syncFromDropbox() }, enabled = !isSyncing) {
                                    Text("Restore from Dropbox")
                                }
                            }
                        }

                        if (lastSyncTime != null) {
                            Text(
                                "Last sync: $lastSyncTime",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
