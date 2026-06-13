package com.vibetodo.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.repository.TodoRepository
import com.vibetodo.presentation.screens.todo.AddEditTodoScreen
import com.vibetodo.presentation.screens.todo.PriorityBadge
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

class CalendarScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repository = koinInject<TodoRepository>()
        val vm = rememberScreenModel { CalendarViewModel(repository) }
        val month by vm.currentMonth.collectAsState()
        val year by vm.currentYear.collectAsState()
        val selectedDay by vm.selectedDay.collectAsState()
        val selectedDayTodos by vm.selectedDayTodos.collectAsState()
        val monthTodoDates by vm.monthTodoDates.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Calendar") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(AddEditTodoScreen()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, "Add Todo")
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { vm.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
                    }
                    Text(
                        text = "${monthName(month)} $year",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { vm.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
                    }
                }

                Spacer(Modifier.height(8.dp))

                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayNames.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                val days = daysInMonth(year, month)
                val firstDayOffset = firstDayOfMonth(year, month)
                val totalCells = firstDayOffset + days
                val rows = (totalCells + 6) / 7
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val day = cellIndex - firstDayOffset + 1
                                if (day in 1..days) {
                                    val isToday = today.year == year && today.monthNumber == month && today.dayOfMonth == day
                                    val isSelected = selectedDay == day
                                    val hasTodos = day in monthTodoDates
                                    DayCell(
                                        modifier = Modifier.weight(1f),
                                        day = day,
                                        isToday = isToday,
                                        isSelected = isSelected,
                                        hasTodos = hasTodos,
                                        onClick = { vm.selectDay(day) },
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (selectedDay != null) {
                    Text(
                        text = "Todos for ${LocalDate(year, month, selectedDay!!)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                }

                if (selectedDayTodos.isEmpty()) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "No todos for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(selectedDayTodos, key = { it.id }) { todo ->
                            CalendarTodoItem(
                                todo = todo,
                                onClick = { navigator.push(AddEditTodoScreen(todo.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasTodos: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
            if (hasTodos) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun CalendarTodoItem(
    todo: Todo,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (todo.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
            PriorityBadge(todo.priority)
        }
    }
}
