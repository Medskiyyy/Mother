package com.mother.app

import android.app.Application
import com.mother.app.di.AppContainer

class MotherApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}