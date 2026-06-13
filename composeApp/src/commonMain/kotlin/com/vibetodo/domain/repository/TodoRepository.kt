package com.vibetodo.domain.repository

import com.vibetodo.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    fun getTodoById(id: String): Todo?
    fun getTodosByCompletion(isCompleted: Boolean): Flow<List<Todo>>
    suspend fun insertTodo(todo: Todo)
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(id: String)
    suspend fun toggleCompletion(id: String)
}
