package com.vibetodo.presentation.screens.calendar

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.vibetodo.domain.model.Todo
import com.vibetodo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CalendarViewModel(
    private val repository: TodoRepository,
) : ScreenModel {

    private val _currentMonth = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber
    )
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _currentYear = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    )
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _selectedDay = MutableStateFlow<Int?>(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth
    )
    val selectedDay: StateFlow<Int?> = _selectedDay.asStateFlow()

    val today: LocalDate
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val selectedDayTodos: StateFlow<List<Todo>> = combine(
        repository.getAllTodos(),
        _selectedDay, _currentMonth, _currentYear,
    ) { allTodos, day, month, year ->
        if (day == null) emptyList()
        else allTodos.filter { todo ->
            todo.dueDate?.let {
                it.year == year && it.monthNumber == month && it.dayOfMonth == day
            } ?: false
        }
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val monthTodoDates: StateFlow<Set<Int>> = combine(
        repository.getAllTodos(),
        _currentMonth, _currentYear,
    ) { allTodos, month, year ->
        allTodos.filter { todo ->
            todo.dueDate?.let { it.year == year && it.monthNumber == month } ?: false
        }.mapNotNull { it.dueDate?.dayOfMonth }.toSet()
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), emptySet())

    fun previousMonth() {
        val m = _currentMonth.value
        val y = _currentYear.value
        if (m == 1) {
            _currentMonth.value = 12
            _currentYear.value = y - 1
        } else {
            _currentMonth.value = m - 1
        }
    }

    fun nextMonth() {
        val m = _currentMonth.value
        val y = _currentYear.value
        if (m == 12) {
            _currentMonth.value = 1
            _currentYear.value = y + 1
        } else {
            _currentMonth.value = m + 1
        }
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }
}

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}

fun firstDayOfMonth(year: Int, month: Int): Int {
    val date = LocalDate(year, month, 1)
    return date.dayOfWeek.ordinal
}

fun monthName(month: Int): String = when (month) {
    1 -> "January"
    2 -> "February"
    3 -> "March"
    4 -> "April"
    5 -> "May"
    6 -> "June"
    7 -> "July"
    8 -> "August"
    9 -> "September"
    10 -> "October"
    11 -> "November"
    12 -> "December"
    else -> "?"
}
