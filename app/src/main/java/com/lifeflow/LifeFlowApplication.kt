package com.lifeflow

import android.app.Application

class LifeFlowApplication : Application() {

    private lateinit var appRuntime: LifeFlowAppRuntime

    override fun onCreate() {
        super.onCreate()
        requireStartupRuntimeEntryPoint().ensureStarted()
    }

    internal fun requireStartupRuntimeEntryPoint(): StartupRuntimeEntryPoint {
        return getOrCreateAppRuntime()
    }

    private fun getOrCreateAppRuntime(): LifeFlowAppRuntime {
        if (!::appRuntime.isInitialized) {
            appRuntime = LifeFlowAppRuntime(applicationContext)
        }
        return appRuntime
    }
}
