package com.lifeflow

import androidx.fragment.app.FragmentActivity

/**
 * Narrow access helper for retrieving the startup runtime entry point
 * from the current application.
 */
internal fun FragmentActivity.requireStartupRuntimeEntryPoint(): StartupRuntimeEntryPoint {
    return (application as LifeFlowApplication).requireStartupRuntimeEntryPoint()
}
