package com.vibetodo.presentation.pomodoro

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PomodoroState {
    Idle, Running, Paused, Break, BreakPaused
}

class PomodoroViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(PomodoroState.Idle)
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _workMinutes = MutableStateFlow(25)
    val workMinutes: StateFlow<Int> = _workMinutes.asStateFlow()

    private val _breakMinutes = MutableStateFlow(5)
    val breakMinutes: StateFlow<Int> = _breakMinutes.asStateFlow()

    private val _longBreakMinutes = MutableStateFlow(15)
    val longBreakMinutes: StateFlow<Int> = _longBreakMinutes.asStateFlow()

    private val _maxIntervals = MutableStateFlow(4)
    val maxIntervals: StateFlow<Int> = _maxIntervals.asStateFlow()

    private val _completedIntervals = MutableStateFlow(0)
    val completedIntervals: StateFlow<Int> = _completedIntervals.asStateFlow()

    private var timerJob: Job? = null

    fun start() {
        val current = _state.value
        if (current != PomodoroState.Idle && current != PomodoroState.Paused && current != PomodoroState.BreakPaused) return
        _state.value = if (current == PomodoroState.Idle || current == PomodoroState.Paused) {
            PomodoroState.Running
        } else {
            PomodoroState.Break
        }
        timerJob = scope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000L)
                _remainingSeconds.value -= 1
            }
            onTimerComplete()
        }
    }

    fun pause() {
        val current = _state.value
        if (current != PomodoroState.Running && current != PomodoroState.Break) return
        timerJob?.cancel()
        _state.value = if (current == PomodoroState.Running) PomodoroState.Paused else PomodoroState.BreakPaused
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _state.value = PomodoroState.Idle
        _remainingSeconds.value = _workMinutes.value * 60
        _completedIntervals.value = 0
    }

    fun updateWorkMinutes(value: Int) {
        _workMinutes.value = value.coerceIn(1, 120)
        if (_state.value == PomodoroState.Idle) {
            _remainingSeconds.value = value * 60
        }
    }

    fun updateBreakMinutes(value: Int) {
        _breakMinutes.value = value.coerceIn(1, 60)
    }

    fun updateLongBreakMinutes(value: Int) {
        _longBreakMinutes.value = value.coerceIn(1, 120)
    }

    fun updateMaxIntervals(value: Int) {
        _maxIntervals.value = value.coerceIn(1, 20)
    }

    private fun onTimerComplete() {
        when (_state.value) {
            PomodoroState.Running -> {
                val completed = _completedIntervals.value + 1
                _completedIntervals.value = completed
                if (completed >= _maxIntervals.value) {
                    _state.value = PomodoroState.Idle
                    _remainingSeconds.value = _workMinutes.value * 60
                } else {
                    val isLongBreak = completed % 4 == 0
                    _remainingSeconds.value = (if (isLongBreak) _longBreakMinutes.value else _breakMinutes.value) * 60
                    _state.value = PomodoroState.Break
                    start()
                }
            }
            PomodoroState.Break -> {
                _remainingSeconds.value = _workMinutes.value * 60
                _state.value = PomodoroState.Running
                start()
            }
            else -> {}
        }
    }

    fun cleanup() {
        timerJob?.cancel()
        scope.cancel()
    }
}
