package com.lifeflow

import android.content.Context
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustAuthority
import kotlin.concurrent.thread

internal fun createDefaultLifeFlowAppRuntimeBindings(
    context: Context,
    isInstrumentation: Boolean
): LifeFlowAppRuntimeBindings {
    return LifeFlowAppGraphFactory.createRuntimeBindings(
        applicationContext = context,
        isInstrumentation = isInstrumentation
    )
}

internal fun launchDefaultLifeFlowAppRuntimeBackgroundTask(
    name: String,
    block: () -> Unit
) {
    thread(start = true, isDaemon = true, name = name) {
        block()
    }
}

internal fun reportDefaultLifeFlowAppRuntimeIntegrityTrustVerdictResponse(
    response: IntegrityTrustVerdictResponse
) {
    SecurityIntegrityTrustAuthority.reportVerdictResponse(response)
}

internal fun createDefaultLifeFlowAppRuntimeStartupIntegrityRequestContext(
    context: Context
): IntegrityStartupRequestContext {
    return buildLifeFlowAppRuntimeStartupIntegrityRequestContext(context)
}
