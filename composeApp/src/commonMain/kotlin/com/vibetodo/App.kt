package com.vibetodo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.presentation.pomodoro.FloatingTimer
import com.vibetodo.presentation.pomodoro.PomodoroViewModel
import com.vibetodo.presentation.screens.calendar.CalendarScreen
import com.vibetodo.presentation.screens.todo.TodoListScreen
import com.vibetodo.presentation.theme.QuluListTheme
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(com.vibetodo.di.appModule)
    }) {
        val helper = koinInject<DatabaseHelper>()
        val scope = rememberCoroutineScope()

        scope.launch {
            helper.createSchema()
        }

        QuluListTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                var selectedTab by remember { mutableStateOf(0) }
                val pomodoroVm = koinInject<PomodoroViewModel>()
                val pomodoroState by pomodoroVm.state.collectAsState()

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.AutoMirrored.Filled.List, "List") },
                                label = { Text("List") },
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.DateRange, "Calendar") },
                                label = { Text("Calendar") },
                            )
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (selectedTab) {
                            0 -> Navigator(TodoListScreen()) { SlideTransition(it) }
                            1 -> Navigator(CalendarScreen()) { SlideTransition(it) }
                        }

                        FloatingTimer(
                            vm = pomodoroVm,
                            onClose = {},
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}
