package com.vibetodo.presentation.screens.todo

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.repository.TodoRepository
import com.vibetodo.domain.usecase.CreateTodoUseCase
import com.vibetodo.domain.usecase.DeleteTodoUseCase
import com.vibetodo.domain.usecase.ToggleTodoUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoDisplayItem(
    val todo: Todo,
    val depth: Int,
    val hasSubtasks: Boolean,
    val isExpanded: Boolean,
)

class TodoListViewModel(
    private val repository: TodoRepository,
    private val createTodoUseCase: CreateTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase,
) : ScreenModel {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(TodoFilter.All)
    val filter: StateFlow<TodoFilter> = _filter.asStateFlow()

    private val _expandedParents = MutableStateFlow<Set<String>>(emptySet())
    val expandedParents: StateFlow<Set<String>> = _expandedParents.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    val displayItems: StateFlow<List<TodoDisplayItem>> = combine(
        repository.getAllTodos(),
        _searchQuery,
        _filter,
        _expandedParents,
    ) { allTodos, query, filter, expanded ->
        val filtered = allTodos
            .filter { todo ->
                when (filter) {
                    TodoFilter.All -> true
                    TodoFilter.Active -> !todo.isCompleted
                    TodoFilter.Completed -> todo.isCompleted
                }
            }
            .filter { todo ->
                query.isBlank() || todo.title.contains(query, ignoreCase = true)
            }

        val parents = filtered.filter { it.parentId == null }
        val subtasks = filtered.filter { it.parentId != null }.groupBy { it.parentId }

        parents.flatMap { parent ->
            val subs = subtasks[parent.id].orEmpty()
            val hasSubtasks = subs.isNotEmpty()
            val isExpanded = parent.id in expanded
            val items = mutableListOf(TodoDisplayItem(parent, 0, hasSubtasks, isExpanded))
            if (isExpanded) {
                subs.forEach { sub ->
                    items.add(TodoDisplayItem(sub, 1, false, false))
                }
            }
            items
        }
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun toggleParentExpanded(parentId: String) {
        val current = _expandedParents.value.toMutableSet()
        if (parentId in current) current.remove(parentId) else current.add(parentId)
        _expandedParents.value = current
    }

    fun setFilter(filter: TodoFilter) { _filter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun toggleCompletion(id: String) {
        screenModelScope.launch { toggleTodoUseCase(id) }
    }

    fun deleteTodo(id: String) {
        screenModelScope.launch {
            deleteTodoUseCase(id)
            _snackbarMessage.tryEmit("Todo deleted")
        }
    }

    fun createQuickTodo(title: String) {
        if (title.isBlank()) return
        screenModelScope.launch {
            createTodoUseCase(title = title)
            _snackbarMessage.tryEmit("Todo created")
        }
    }

    fun getTodoById(id: String): Todo? = repository.getTodoById(id)
}

enum class TodoFilter { All, Active, Completed }
