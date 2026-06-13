package com.vibetodo.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.vibetodo.TodoEntity
import com.vibetodo.db.TodoDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DatabaseHelper(private val driver: SqlDriver) {
    private val database = TodoDb(driver)
    private val queries = database.todoDbQueries

    suspend fun createSchema() {
        withContext(Dispatchers.IO) {
            TodoDb.Schema.create(driver)
        }
    }

    fun getAllTodos(): Flow<List<TodoEntity>> {
        return queries.selectAll().asFlow().mapToList(Dispatchers.IO)
    }

    fun getTodoById(id: String): TodoEntity? {
        return queries.selectById(id).executeAsOneOrNull()
    }

    fun getTodosByCompletion(isCompleted: Boolean): Flow<List<TodoEntity>> {
        return queries.selectByCompletion(if (isCompleted) 1L else 0L)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    suspend fun insert(
        id: String,
        title: String,
        description: String,
        dueDate: Long?,
        dueTimeMinutes: Long?,
        priority: String,
        categoryId: String?,
        isCompleted: Long,
        isRecurring: Long,
        recurrenceRule: String?,
        createdAt: Long,
        updatedAt: Long,
    ) = withContext(Dispatchers.IO) {
        queries.insert(
            id = id,
            title = title,
            description = description,
            dueDate = dueDate,
            dueTimeMinutes = dueTimeMinutes,
            priority = priority,
            categoryId = categoryId,
            isCompleted = isCompleted,
            isRecurring = isRecurring,
            recurrenceRule = recurrenceRule,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    suspend fun update(
        id: String,
        title: String,
        description: String,
        dueDate: Long?,
        dueTimeMinutes: Long?,
        priority: String,
        categoryId: String?,
        isCompleted: Long,
        isRecurring: Long,
        recurrenceRule: String?,
        updatedAt: Long,
    ) = withContext(Dispatchers.IO) {
        queries.update(
            title = title,
            description = description,
            dueDate = dueDate,
            dueTimeMinutes = dueTimeMinutes,
            priority = priority,
            categoryId = categoryId,
            isCompleted = isCompleted,
            isRecurring = isRecurring,
            recurrenceRule = recurrenceRule,
            updatedAt = updatedAt,
            id = id,
        )
    }

    suspend fun deleteById(id: String) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }
}
