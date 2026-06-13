package com.vibetodo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.presentation.screens.todo.TodoListScreen
import com.vibetodo.presentation.theme.VibeTodoTheme
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

        VibeTodoTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Navigator(TodoListScreen()) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
}
