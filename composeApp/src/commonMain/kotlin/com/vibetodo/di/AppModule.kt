package com.vibetodo.di

import app.cash.sqldelight.db.SqlDriver
import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.data.local.createSqlDriver
import com.vibetodo.data.repository.TodoRepositoryImpl
import com.vibetodo.domain.repository.TodoRepository
import com.vibetodo.domain.usecase.CreateTodoUseCase
import com.vibetodo.domain.usecase.DeleteTodoUseCase
import com.vibetodo.domain.usecase.ToggleTodoUseCase
import com.vibetodo.domain.usecase.UpdateTodoUseCase
import com.vibetodo.presentation.pomodoro.PomodoroViewModel
import org.koin.dsl.module

val appModule = module {
    single<SqlDriver> { createSqlDriver() }
    single { DatabaseHelper(get()) }
    single<TodoRepository> { TodoRepositoryImpl(get()) }
    single { PomodoroViewModel() }
    factory { CreateTodoUseCase(get()) }
    factory { UpdateTodoUseCase(get()) }
    factory { DeleteTodoUseCase(get()) }
    factory { ToggleTodoUseCase(get()) }
}
