package com.mother.app

import android.app.Application
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
    }
}
