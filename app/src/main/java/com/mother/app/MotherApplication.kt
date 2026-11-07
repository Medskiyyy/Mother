package com.mother.app

import android.app.Application
import com.mother.app.data.reminder.BootReceiver
import com.mother.app.data.timer.ActiveTimerStore
import com.mother.app.di.AppContainer
import com.mother.app.widget.MotherWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
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
        // A backup restore replaces the database, so its reminders must be re-armed.
        applicationScope.launch { BootReceiver.rescheduleIfPending(this@MotherApplication, container) }
        // Keep the home-screen widget in step with study sessions.
        applicationScope.launch {
            container.studySessionRepository.observeAllAsc().drop(1).collect {
                MotherWidgetProvider.requestUpdate(this@MotherApplication)
            }
        }
        // Restore the active timer after process death. The foreground service is
        // started from MainActivity: this process may also be created in the
        // background (boot, alarm, widget), where Android 12+ forbids starting it.
        ActiveTimerStore.init(this)
    }
}
