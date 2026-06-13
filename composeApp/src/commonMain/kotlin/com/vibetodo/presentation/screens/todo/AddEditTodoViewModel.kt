package com.vibetodo.presentation.screens.todo

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.vibetodo.domain.model.Priority
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.repository.TodoRepository
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
    private val initialParentId: String?,
    private val createTodoUseCase: CreateTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val todoRepository: TodoRepository,
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

    private val _isRecurring = MutableStateFlow(false)
    val isRecurring: StateFlow<Boolean> = _isRecurring.asStateFlow()

    private val _recurrenceRule = MutableStateFlow("WEEKLY")
    val recurrenceRule: StateFlow<String> = _recurrenceRule.asStateFlow()

    private val _parentTodoId = MutableStateFlow(initialParentId)
    val parentTodoId: StateFlow<String?> = _parentTodoId.asStateFlow()

    private val _availableParents = MutableStateFlow<List<Todo>>(emptyList())
    val availableParents: StateFlow<List<Todo>> = _availableParents.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        if (todoId != null) {
            todoRepository.getTodoById(todoId)?.let { todo ->
                _title.value = todo.title
                _description.value = todo.description
                _dueDate.value = todo.dueDate
                _dueTime.value = todo.dueTime
                _priority.value = todo.priority
                _isRecurring.value = todo.isRecurring
                _recurrenceRule.value = todo.recurrenceRule ?: "WEEKLY"
                _parentTodoId.value = todo.parentId
            }
        }
        screenModelScope.launch {
            todoRepository.getAllTodos().collect { allTodos ->
                val parents = allTodos.filter { it.parentId == null && it.id != todoId }
                _availableParents.value = parents
            }
        }
    }

    fun updateTitle(value: String) { _title.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updateDueDate(value: LocalDate?) { _dueDate.value = value }
    fun updateDueTime(value: LocalTime?) { _dueTime.value = value }
    fun updatePriority(value: Priority) { _priority.value = value }
    fun updateIsRecurring(value: Boolean) { _isRecurring.value = value }
    fun updateRecurrenceRule(value: String) { _recurrenceRule.value = value }
    fun updateParentTodoId(value: String?) { _parentTodoId.value = value }

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
                    parentId = _parentTodoId.value,
                    isRecurring = _isRecurring.value,
                    recurrenceRule = if (_isRecurring.value) _recurrenceRule.value else null,
                )
            } else {
                val existing = todoRepository.getTodoById(todoId) ?: return@launch
                updateTodoUseCase(
                    existing.copy(
                        title = _title.value.trim(),
                        description = _description.value.trim(),
                        dueDate = _dueDate.value,
                        dueTime = _dueTime.value,
                        priority = _priority.value,
                        parentId = _parentTodoId.value,
                        isRecurring = _isRecurring.value,
                        recurrenceRule = if (_isRecurring.value) _recurrenceRule.value else null,
                    )
                )
            }
            _isSaving.value = false
            _isSaved.value = true
        }
    }
}
