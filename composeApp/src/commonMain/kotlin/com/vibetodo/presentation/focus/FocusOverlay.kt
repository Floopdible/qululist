package com.vibetodo.presentation.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibetodo.presentation.pomodoro.AmbientSound
import com.vibetodo.presentation.pomodoro.PomodoroViewModel
import com.vibetodo.presentation.pomodoro.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusOverlay(
    vm: PomodoroViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsState()
    val remaining by vm.remainingSeconds.collectAsState()
    val ambientSound by vm.ambientSound.collectAsState()
    val completed by vm.completedIntervals.collectAsState()
    val maxIntervals by vm.maxIntervals.collectAsState()
    var soundExpanded by remember { mutableStateOf(false) }
    val streak = remember { vm.calculateStreak() }
    val todayMin = remember { vm.todayMinutes() }

    val greeting = when {
        streak > 0 -> "🔥 $streak-day streak!"
        else -> "Let's focus!"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.width(340.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Today: ${todayMin}m focused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = formatTime(remaining),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                val statusText = when (state) {
                    com.vibetodo.presentation.pomodoro.PomodoroState.Running -> "Focus"
                    com.vibetodo.presentation.pomodoro.PomodoroState.Break -> "Break"
                    com.vibetodo.presentation.pomodoro.PomodoroState.Paused -> "Paused"
                    com.vibetodo.presentation.pomodoro.PomodoroState.BreakPaused -> "Break Paused"
                    com.vibetodo.presentation.pomodoro.PomodoroState.Idle -> ""
                }
                if (statusText.isNotBlank()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Text(
                    text = "Interval $completed / $maxIntervals",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(20.dp))

                ExposedDropdownMenuBox(
                    expanded = soundExpanded,
                    onExpandedChange = { soundExpanded = !soundExpanded },
                ) {
                    OutlinedTextField(
                        value = ambientSound.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ambient Sound") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = soundExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(expanded = soundExpanded, onDismissRequest = { soundExpanded = false }) {
                        AmbientSound.entries.forEach { sound ->
                            DropdownMenuItem(
                                text = { Text(sound.label) },
                                onClick = {
                                    vm.updateAmbientSound(sound)
                                    soundExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Back to app")
                }
            }
        }
    }
}
