package com.mother.app

import android.app.Application
import com.mother.app.data.timer.ActiveTimerStore
import com.mother.app.data.timer.TimerPhase
import com.mother.app.data.timer.TimerService
import com.mother.app.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MotherApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Reconcile schedule statuses missed while the app was closed (PRD §12).
        applicationScope.launch { container.scheduleStatusSyncer.syncNow() }
        // Restore the active timer after process death; the foreground service
        // keeps it alive (PRD §16: timer keeps running when the app is closed).
        // Only a RUNNING timer gets the foreground notification — starting the
        // service for a paused timer flashes a notification that immediately
        // disappears, so paused timers stay silent.
        ActiveTimerStore.init(this)
        if (ActiveTimerStore.activeTimer.value?.phase == TimerPhase.RUNNING) {
            TimerService.start(this)
        }
    }
}
