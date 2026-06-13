package com.vibetodo.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.vibetodo.FocusSessionEntity
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

    fun getAllTodosSnapshot(): List<TodoEntity> {
        return queries.selectAll().executeAsList()
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

    suspend fun deleteAllTodos() = withContext(Dispatchers.IO) {
        queries.deleteAllTodos()
    }

    suspend fun insertAll(todos: List<TodoEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
            todos.forEach { todo ->
                queries.insert(
                    id = todo.id,
                    title = todo.title,
                    description = todo.description,
                    dueDate = todo.dueDate,
                    dueTimeMinutes = todo.dueTimeMinutes,
                    priority = todo.priority,
                    categoryId = todo.categoryId,
                    isCompleted = todo.isCompleted,
                    isRecurring = todo.isRecurring,
                    recurrenceRule = todo.recurrenceRule,
                    createdAt = todo.createdAt,
                    updatedAt = todo.updatedAt,
                )
            }
        }
    }

    fun getAllSessions(): List<FocusSessionEntity> {
        return queries.selectAllSessions().executeAsList()
    }

    fun getSessionsSince(sinceEpochMillis: Long): List<FocusSessionEntity> {
        return queries.selectSessionsSince(sinceEpochMillis).executeAsList()
    }

    suspend fun insertSession(
        id: String,
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        completed: Long,
    ) = withContext(Dispatchers.IO) {
        queries.insertSession(
            id = id,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes.toLong(),
            completed = completed,
        )
    }

    fun getConfig(key: String): String? {
        return queries.getConfig(key).executeAsOneOrNull()
    }

    suspend fun setConfig(key: String, value: String) = withContext(Dispatchers.IO) {
        queries.setConfig(key, value)
    }

    suspend fun deleteConfig(key: String) = withContext(Dispatchers.IO) {
        queries.deleteConfig(key)
    }
}
