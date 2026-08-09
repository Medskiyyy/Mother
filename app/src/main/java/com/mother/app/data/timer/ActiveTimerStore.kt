package com.mother.app.data.timer

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TimerPhase { IDLE, RUNNING, PAUSED }

/** Snapshot of the single active study timer (PRD §16). */
data class ActiveTimer(
    val habitId: String,
    val habitTitle: String,
    val targetMinute: Int,
    val phase: TimerPhase,
    /** Epoch millis when the current run segment started (0 while paused). */
    val segmentStart: Long,
    /** Milliseconds accumulated across segments before the current one. */
    val accumulatedMillis: Long
)

/**
 * Persistent, epoch-based state of the single active timer (PRD §16: only one
 * timer at a time; AGENT_RULES §11). Elapsed time is always derived from
 * [System.currentTimeMillis], so it keeps counting through app restarts,
 * screen-off, and process death — pausing simply stops adding time.
 */
object ActiveTimerStore {

    private const val PREFS = "mother_timer"
    private const val KEY_HABIT_ID = "habitId"
    private const val KEY_HABIT_TITLE = "habitTitle"
    private const val KEY_TARGET_MINUTE = "targetMinute"
    private const val KEY_PHASE = "phase"
    private const val KEY_SEGMENT_START = "segmentStart"
    private const val KEY_ACCUMULATED_MILLIS = "accumulatedMillis"

    private val _activeTimer = MutableStateFlow<ActiveTimer?>(null)
    /** The single active timer; null when idle. */
    val activeTimer: StateFlow<ActiveTimer?> = _activeTimer.asStateFlow()

    private var prefs: SharedPreferences? = null

    /** Restores persisted timer state on app start. */
    fun init(context: Context) {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = preferences
        val habitId = preferences.getString(KEY_HABIT_ID, null) ?: return
        val phase = runCatching {
            TimerPhase.valueOf(preferences.getString(KEY_PHASE, TimerPhase.RUNNING.name).orEmpty())
        }.getOrDefault(TimerPhase.RUNNING)
        _activeTimer.value = ActiveTimer(
            habitId = habitId,
            habitTitle = preferences.getString(KEY_HABIT_TITLE, "").orEmpty(),
            targetMinute = preferences.getInt(KEY_TARGET_MINUTE, 0),
            phase = phase,
            segmentStart = preferences.getLong(KEY_SEGMENT_START, 0L),
            accumulatedMillis = preferences.getLong(KEY_ACCUMULATED_MILLIS, 0L)
        )
    }

    fun start(habitId: String, habitTitle: String, targetMinute: Int, now: Long = System.currentTimeMillis()) {
        set(ActiveTimer(habitId, habitTitle, targetMinute, TimerPhase.RUNNING, now, 0L))
    }

    fun pause(now: Long = System.currentTimeMillis()) {
        val current = _activeTimer.value ?: return
        if (current.phase != TimerPhase.RUNNING) return
        val totalMillis = elapsedMillis(now)
        set(current.copy(phase = TimerPhase.PAUSED, segmentStart = 0L, accumulatedMillis = totalMillis))
    }

    fun resume(now: Long = System.currentTimeMillis()) {
        val current = _activeTimer.value ?: return
        if (current.phase != TimerPhase.PAUSED) return
        set(current.copy(phase = TimerPhase.RUNNING, segmentStart = now))
    }

    /** Final elapsed minutes and the last snapshot; clears the active timer. */
    fun stop(now: Long = System.currentTimeMillis()): Pair<Long, ActiveTimer>? {
        val current = _activeTimer.value ?: return null
        val minutes = elapsedMinute(now)
        clear()
        return minutes to current
    }

    /** Elapsed total milliseconds including the running segment. */
    fun elapsedMillis(now: Long = System.currentTimeMillis()): Long {
        val current = _activeTimer.value ?: return 0L
        val segment = if (current.phase == TimerPhase.RUNNING) (now - current.segmentStart).coerceAtLeast(0L) else 0L
        return current.accumulatedMillis + segment
    }

    /** Elapsed whole minutes including the running segment. */
    fun elapsedMinute(now: Long = System.currentTimeMillis()): Long {
        return elapsedMillis(now) / 60_000L
    }

    /** Remaining minutes toward the habit's daily target, floored at zero. */
    fun remainingMinute(now: Long = System.currentTimeMillis()): Long {
        val current = _activeTimer.value ?: return 0L
        return (current.targetMinute - elapsedMinute(now)).coerceAtLeast(0L)
    }

    fun clear() {
        _activeTimer.value = null
        prefs?.edit()?.clear()?.apply()
    }

    private fun set(timer: ActiveTimer) {
        _activeTimer.value = timer
        prefs?.edit()
            ?.putString(KEY_HABIT_ID, timer.habitId)
            ?.putString(KEY_HABIT_TITLE, timer.habitTitle)
            ?.putInt(KEY_TARGET_MINUTE, timer.targetMinute)
            ?.putString(KEY_PHASE, timer.phase.name)
            ?.putLong(KEY_SEGMENT_START, timer.segmentStart)
            ?.putLong(KEY_ACCUMULATED_MILLIS, timer.accumulatedMillis)
            ?.apply()
    }
}
