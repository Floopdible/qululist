package com.vibetodo.data.repository

import com.vibetodo.TodoEntity
import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.domain.model.Priority
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

class TodoRepositoryImpl(private val helper: DatabaseHelper) : TodoRepository {

    override fun getAllTodos(): Flow<List<Todo>> {
        return helper.getAllTodos().map { list -> list.map { it.toDomain() } }
    }

    override fun getTodoById(id: String): Todo? {
        return helper.getTodoById(id)?.toDomain()
    }

    override fun getTodosByCompletion(isCompleted: Boolean): Flow<List<Todo>> {
        return helper.getTodosByCompletion(isCompleted).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTodo(todo: Todo) {
        helper.insert(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            dueDate = todo.dueDate?.toEpochMillis(),
            dueTimeMinutes = todo.dueTime?.let { (it.hour * 60L + it.minute) },
            priority = todo.priority.name,
            categoryId = todo.categoryId,
            isCompleted = if (todo.isCompleted) 1L else 0L,
            isRecurring = if (todo.isRecurring) 1L else 0L,
            recurrenceRule = todo.recurrenceRule,
            createdAt = todo.createdAt.toEpochMilliseconds(),
            updatedAt = todo.updatedAt.toEpochMilliseconds(),
        )
    }

    override suspend fun updateTodo(todo: Todo) {
        helper.update(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            dueDate = todo.dueDate?.toEpochMillis(),
            dueTimeMinutes = todo.dueTime?.let { (it.hour * 60L + it.minute) },
            priority = todo.priority.name,
            categoryId = todo.categoryId,
            isCompleted = if (todo.isCompleted) 1L else 0L,
            isRecurring = if (todo.isRecurring) 1L else 0L,
            recurrenceRule = todo.recurrenceRule,
            updatedAt = todo.updatedAt.toEpochMilliseconds(),
        )
    }

    override suspend fun deleteTodo(id: String) {
        helper.deleteById(id)
    }

    override suspend fun toggleCompletion(id: String) {
        val todo = getTodoById(id) ?: return
        updateTodo(todo.copy(isCompleted = !todo.isCompleted, updatedAt = Clock.System.now()))
    }
}

private fun TodoEntity.toDomain(): Todo {
    val date = dueDate?.let { millis ->
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val time = dueTimeMinutes?.toInt()?.let { totalMinutes ->
        LocalTime(totalMinutes / 60, totalMinutes % 60)
    }
    return Todo(
        id = id,
        title = title,
        description = description,
        dueDate = date,
        dueTime = time,
        priority = Priority.fromString(priority),
        categoryId = categoryId,
        isCompleted = isCompleted != 0L,
        isRecurring = isRecurring != 0L,
        recurrenceRule = recurrenceRule,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    )
}

private fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}
