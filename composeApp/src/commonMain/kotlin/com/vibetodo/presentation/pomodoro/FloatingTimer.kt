package com.vibetodo.presentation.pomodoro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun FloatingTimer(
    vm: PomodoroViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsState()
    val remaining by vm.remainingSeconds.collectAsState()
    val workMinutes by vm.workMinutes.collectAsState()
    val breakMinutes by vm.breakMinutes.collectAsState()
    val longBreakMinutes by vm.longBreakMinutes.collectAsState()
    val maxIntervals by vm.maxIntervals.collectAsState()
    val completed by vm.completedIntervals.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var collapsed by remember { mutableStateOf(false) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    val dragModifier = Modifier
        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                dragX += dragAmount.x
                dragY += dragAmount.y
            }
        }

    if (collapsed) {
        Card(
            modifier = modifier
                .then(dragModifier)
                .size(48.dp)
                .clip(CircleShape)
                .clickable { collapsed = false },
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "🍅",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        return
    }

    Card(
        modifier = modifier
            .then(dragModifier)
            .width(220.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🍅 Pomodoro", style = MaterialTheme.typography.labelLarge)
                Row {
                    IconButton(onClick = { showSettings = !showSettings }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Settings, "Settings", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = {
                        collapsed = true
                        onClose()
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Collapse", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text(
                text = formatTime(remaining),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            val statusText = when (state) {
                PomodoroState.Idle -> "Ready"
                PomodoroState.Running -> "Focus"
                PomodoroState.Paused -> "Paused"
                PomodoroState.Break -> "Break"
                PomodoroState.BreakPaused -> "Break Paused"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$completed / $maxIntervals intervals",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (state) {
                    PomodoroState.Idle -> {
                        OutlinedButton(onClick = { vm.start() }) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Start")
                        }
                    }
                    PomodoroState.Running, PomodoroState.Break -> {
                        OutlinedButton(onClick = { vm.pause() }) {
                            Text("Pause")
                        }
                    }
                    PomodoroState.Paused, PomodoroState.BreakPaused -> {
                        OutlinedButton(onClick = { vm.start() }) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }
                }
                if (state != PomodoroState.Idle) {
                    OutlinedButton(onClick = { vm.stop() }) {
                        Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Stop")
                    }
                }
            }

            if (showSettings) {
                Spacer(Modifier.height(8.dp))
                SettingsRow("Work", workMinutes) { vm.updateWorkMinutes(it) }
                SettingsRow("Break", breakMinutes) { vm.updateBreakMinutes(it) }
                SettingsRow("Long Brk", longBreakMinutes) { vm.updateLongBreakMinutes(it) }
                SettingsRow("Sessions", maxIntervals) { vm.updateMaxIntervals(it) }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { it.toIntOrNull()?.let(onValueChange) },
            modifier = Modifier.width(60.dp).height(40.dp),
            singleLine = true,
        )
    }
}

fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
