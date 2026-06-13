package com.vibetodo

import kotlin.Long
import kotlin.String

public data class TodoEntity(
  public val id: String,
  public val title: String,
  public val description: String,
  public val dueDate: Long?,
  public val dueTimeMinutes: Long?,
  public val priority: String,
  public val categoryId: String?,
  public val isCompleted: Long,
  public val isRecurring: Long,
  public val recurrenceRule: String?,
  public val createdAt: Long,
  public val updatedAt: Long,
)
