package com.vibetodo.presentation.screens.todo

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.vibetodo.domain.model.Priority
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.usecase.CreateTodoUseCase
import com.vibetodo.domain.usecase.UpdateTodoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class AddEditTodoViewModel(
    private val todoId: String?,
    private val createTodoUseCase: CreateTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTodo: (String) -> Todo?,
) : ScreenModel {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _dueDate = MutableStateFlow<LocalDate?>(null)
    val dueDate: StateFlow<LocalDate?> = _dueDate.asStateFlow()

    private val _dueTime = MutableStateFlow<LocalTime?>(null)
    val dueTime: StateFlow<LocalTime?> = _dueTime.asStateFlow()

    private val _priority = MutableStateFlow(Priority.Medium)
    val priority: StateFlow<Priority> = _priority.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        todoId?.let { id ->
            getTodo(id)?.let { todo ->
                _title.value = todo.title
                _description.value = todo.description
                _dueDate.value = todo.dueDate
                _dueTime.value = todo.dueTime
                _priority.value = todo.priority
            }
        }
    }

    fun updateTitle(value: String) { _title.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updateDueDate(value: LocalDate?) { _dueDate.value = value }
    fun updateDueTime(value: LocalTime?) { _dueTime.value = value }
    fun updatePriority(value: Priority) { _priority.value = value }

    private fun validate(): Boolean = _title.value.isNotBlank()

    fun save() {
        if (!validate()) return
        _isSaving.value = true
        screenModelScope.launch {
            if (todoId == null) {
                createTodoUseCase(
                    title = _title.value.trim(),
                    description = _description.value.trim(),
                    dueDate = _dueDate.value,
                    dueTime = _dueTime.value,
                    priority = _priority.value,
                )
            } else {
                val existing = getTodo(todoId) ?: return@launch
                updateTodoUseCase(
                    existing.copy(
                        title = _title.value.trim(),
                        description = _description.value.trim(),
                        dueDate = _dueDate.value,
                        dueTime = _dueTime.value,
                        priority = _priority.value,
                    )
                )
            }
            _isSaving.value = false
            _isSaved.value = true
        }
    }
}
