package com.vibetodo.domain.usecase

import com.vibetodo.domain.model.IdGenerator
import com.vibetodo.domain.model.Priority
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.repository.TodoRepository
import kotlinx.datetime.Clock

class CreateTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        dueDate: kotlinx.datetime.LocalDate? = null,
        dueTime: kotlinx.datetime.LocalTime? = null,
        priority: Priority = Priority.Medium,
        categoryId: String? = null,
        parentId: String? = null,
        isRecurring: Boolean = false,
        recurrenceRule: String? = null,
    ) {
        val now = Clock.System.now()
        val todo = Todo(
            id = IdGenerator.newId(),
            title = title,
            description = description,
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            categoryId = categoryId,
            parentId = parentId,
            isCompleted = false,
            isRecurring = isRecurring,
            recurrenceRule = recurrenceRule,
            createdAt = now,
            updatedAt = now,
        )
        repository.insertTodo(todo)
    }
}

class UpdateTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(todo: Todo) {
        repository.updateTodo(todo.copy(updatedAt = Clock.System.now()))
    }
}

class DeleteTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(id: String) {
        repository.deleteTodo(id)
    }
}

class ToggleTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(id: String) {
        repository.toggleCompletion(id)
    }
}
