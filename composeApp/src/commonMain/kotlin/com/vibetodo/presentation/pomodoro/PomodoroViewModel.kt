package com.vibetodo.presentation.pomodoro

import com.vibetodo.data.local.DatabaseHelper
import com.vibetodo.domain.model.IdGenerator
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class PomodoroState {
    Idle, Running, Paused, Break, BreakPaused
}

enum class AmbientSound(val label: String) {
    None("Silence"),
    Rain("Rain"),
    Ocean("Ocean"),
    Forest("Forest"),
    WhiteNoise("White Noise"),
}

class PomodoroViewModel(
    private val dbHelper: DatabaseHelper,
) {

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

    private val _isFocusMode = MutableStateFlow(false)
    val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

    private val _ambientSound = MutableStateFlow(AmbientSound.None)
    val ambientSound: StateFlow<AmbientSound> = _ambientSound.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: Instant? = null

    fun start() {
        val current = _state.value
        if (current != PomodoroState.Idle && current != PomodoroState.Paused && current != PomodoroState.BreakPaused) return
        _state.value = if (current == PomodoroState.Idle || current == PomodoroState.Paused) {
            if (current == PomodoroState.Idle) {
                _isFocusMode.value = true
                sessionStartTime = Clock.System.now()
            }
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
        _isFocusMode.value = false
        logIncompleteSession()
        sessionStartTime = null
    }

    fun dismissFocus() {
        _isFocusMode.value = false
    }

    fun updateAmbientSound(sound: AmbientSound) {
        _ambientSound.value = sound
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

    fun calculateStreak(): Int {
        val sessions = dbHelper.getAllSessions()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var streak = 0
        var expected = today
        val completedByDate = sessions
            .filter { it.completed != 0L }
            .groupBy {
                Instant.fromEpochMilliseconds(it.startTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            .mapValues { it.value.size }
            .toSortedMap(compareByDescending { it })
        for ((date, _) in completedByDate) {
            if (date == expected) {
                streak++
                expected = expected - DatePeriod(days = 1)
            } else if (date < expected && streak == 0) {
                expected = date
                streak = 1
                expected = expected - DatePeriod(days = 1)
            } else {
                break
            }
        }
        return streak
    }

    fun todayMinutes(): Int {
        val now = Clock.System.now()
        val startOfDay = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            .atStartOfDayIn(TimeZone.currentSystemDefault())
        val sessions = dbHelper.getSessionsSince(startOfDay.toEpochMilliseconds())
        return sessions.sumOf { it.durationMinutes.toInt() }
    }

    private fun onTimerComplete() {
        when (_state.value) {
            PomodoroState.Running -> {
                logCompletedSession()
                sessionStartTime = Clock.System.now()
                val completed = _completedIntervals.value + 1
                _completedIntervals.value = completed
                if (completed >= _maxIntervals.value) {
                    _state.value = PomodoroState.Idle
                    _remainingSeconds.value = _workMinutes.value * 60
                    _isFocusMode.value = false
                    sessionStartTime = null
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

    private fun logCompletedSession() {
        val start = sessionStartTime ?: return
        val end = Clock.System.now()
        val durationMinutes = ((end.toEpochMilliseconds() - start.toEpochMilliseconds()) / 60000).toInt().coerceAtLeast(1)
        scope.launch {
            dbHelper.insertSession(
                id = IdGenerator.newId(),
                startTime = start.toEpochMilliseconds(),
                endTime = end.toEpochMilliseconds(),
                durationMinutes = durationMinutes,
                completed = 1L,
            )
        }
    }

    private fun logIncompleteSession() {
        val start = sessionStartTime ?: return
        val end = Clock.System.now()
        val durationSeconds = (end.toEpochMilliseconds() - start.toEpochMilliseconds()) / 1000
        if (durationSeconds < 60) return
        val durationMinutes = (durationSeconds / 60).toInt().coerceAtLeast(1)
        scope.launch {
            dbHelper.insertSession(
                id = IdGenerator.newId(),
                startTime = start.toEpochMilliseconds(),
                endTime = end.toEpochMilliseconds(),
                durationMinutes = durationMinutes,
                completed = 0L,
            )
        }
    }

    fun cleanup() {
        timerJob?.cancel()
        scope.cancel()
    }
}
