package com.vibetodo.presentation.screens.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vibetodo.domain.model.Priority
import com.vibetodo.domain.repository.TodoRepository
import com.vibetodo.domain.usecase.CreateTodoUseCase
import com.vibetodo.domain.usecase.UpdateTodoUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

data class AddEditTodoScreen(val todoId: String? = null) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val createUC = koinInject<CreateTodoUseCase>()
        val updateUC = koinInject<UpdateTodoUseCase>()
        val todoRepo = koinInject<TodoRepository>()
        val screenModel = rememberScreenModel { AddEditTodoViewModel(todoId, createUC, updateUC, { todoRepo.getTodoById(it) }) }
        val title by screenModel.title.collectAsState()
        val description by screenModel.description.collectAsState()
        val dueDate by screenModel.dueDate.collectAsState()
        val dueTime by screenModel.dueTime.collectAsState()
        val priority by screenModel.priority.collectAsState()
        val isRecurring by screenModel.isRecurring.collectAsState()
        val recurrenceRule by screenModel.recurrenceRule.collectAsState()
        val isSaving by screenModel.isSaving.collectAsState()
        val isSaved by screenModel.isSaved.collectAsState()

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var priorityExpanded by remember { mutableStateOf(false) }
        var recurringExpanded by remember { mutableStateOf(false) }

        if (isSaved) {
            navigator.pop()
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (todoId == null) "New Todo" else "Edit Todo") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = screenModel::updateTitle,
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = screenModel::updateDescription,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3,
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(dueDate?.toString() ?: "Pick Date")
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(dueTime?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: "Pick Time")
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded },
                ) {
                    OutlinedTextField(
                        value = priority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                        Priority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = {
                                    screenModel.updatePriority(p)
                                    priorityExpanded = false
                                },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Recurring", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = screenModel::updateIsRecurring,
                    )
                }

                if (isRecurring) {
                    val recurrenceOptions = listOf("DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly")
                    ExposedDropdownMenuBox(
                        expanded = recurringExpanded,
                        onExpandedChange = { recurringExpanded = !recurringExpanded },
                    ) {
                        OutlinedTextField(
                            value = recurrenceOptions.first { it.first == recurrenceRule }.second,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Repeat") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurringExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(expanded = recurringExpanded, onDismissRequest = { recurringExpanded = false }) {
                            recurrenceOptions.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        screenModel.updateRecurrenceRule(code)
                                        recurringExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = screenModel::save,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isSaving,
                ) {
                    Text(if (isSaving) "Saving..." else "Save")
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = dueDate?.let {
                    it.atStartOfDayIn(kotlinx.datetime.TimeZone.UTC).toEpochMilliseconds()
                } ?: kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            screenModel.updateDueDate(
                                kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
                                    .toLocalDateTime(kotlinx.datetime.TimeZone.UTC).date
                            )
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                },
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val timeState = rememberTimePickerState(
                initialHour = dueTime?.hour ?: 12,
                initialMinute = dueTime?.minute ?: 0,
                is24Hour = true,
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = { Text("Select Time") },
                text = { TimePicker(state = timeState) },
                confirmButton = {
                    TextButton(onClick = {
                        screenModel.updateDueTime(LocalTime(timeState.hour, timeState.minute))
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
            )
        }
    }
}
