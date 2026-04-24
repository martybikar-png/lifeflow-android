package com.lifeflow

import com.lifeflow.security.SecurityAuthPerUseCryptoProvider

internal fun evaluateLifeFlowAppRuntimeProtectedAccess(
    startupInitLock: Any,
    startupInitialized: Boolean,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?,
    runtimeSecuritySurveillanceCoordinator: RuntimeSecuritySurveillanceCoordinator
) {
    val snapshot = synchronized(startupInitLock) {
        createLifeFlowAppRuntimeProtectedAccessSnapshot(
            startupInitialized = startupInitialized,
            hardeningReportAvailable = runtimeBindingsOrNull()?.hardeningReport != null
        )
    }

    evaluateLifeFlowAppRuntimeProtectedAccessSecurityRecoveryIfNeeded(
        snapshot = snapshot,
        runtimeSecuritySurveillanceCoordinator = runtimeSecuritySurveillanceCoordinator
    )
}

internal fun requireLifeFlowAppRuntimeMainViewModelFactory(
    evaluateProtectedAccessSecurityRecoveryIfNeeded: () -> Unit,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?
): MainViewModelFactory {
    evaluateProtectedAccessSecurityRecoveryIfNeeded()
    return requireLifeFlowAppRuntimeBindings(
        runtimeBindingsOrNull = runtimeBindingsOrNull
    ).mainViewModelFactory
}

internal fun requireLifeFlowAppRuntimeAuthPerUseCryptoProviderOrNull(
    evaluateProtectedAccessSecurityRecoveryIfNeeded: () -> Unit,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?
): SecurityAuthPerUseCryptoProvider? {
    evaluateProtectedAccessSecurityRecoveryIfNeeded()
    return requireLifeFlowAppRuntimeBindings(
        runtimeBindingsOrNull = runtimeBindingsOrNull
    ).authPerUseCryptoProvider
}

internal fun readLifeFlowAppRuntimeStartupFailureMessage(
    startupFailureMessage: String?
): String? = startupFailureMessage
