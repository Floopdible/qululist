package com.vibetodo.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Todo(
    val id: String,
    val title: String,
    val description: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val priority: Priority = Priority.Medium,
    val categoryId: String? = null,
    val parentId: String? = null,
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST,
)

enum class Priority {
    Low,
    Medium,
    High,
    Urgent;

    companion object {
        fun fromString(value: String): Priority =
            entries.firstOrNull { it.name == value } ?: Medium
    }
}

object IdGenerator {
    fun newId(): String {
        val hex = "abcdef0123456789"
        return buildString {
            repeat(8) { append(hex.random()) }
            append('-')
            repeat(4) { append(hex.random()) }
            append('-')
            repeat(4) { append(hex.random()) }
            append('-')
            repeat(4) { append(hex.random()) }
            append('-')
            repeat(12) { append(hex.random()) }
        }
    }
}
