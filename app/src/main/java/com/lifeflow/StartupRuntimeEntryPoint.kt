package com.lifeflow

import com.lifeflow.security.IntegrityTrustRuntime
import com.lifeflow.security.SecurityAuthPerUseCryptoProvider

/**
 * Narrow startup/runtime surface for UI and activity entry points.
 *
 * Purpose:
 * - keep UI unaware of concrete app runtime implementation
 * - expose only startup retry, failure reading, and runtime entry access points
 */
internal interface StartupRuntimeEntryPoint {
    fun ensureStarted(): Boolean
    fun requireMainViewModelFactory(): MainViewModelFactory
    fun authPerUseCryptoProviderOrNull(): SecurityAuthPerUseCryptoProvider?
    fun requireIntegrityTrustRuntime(): IntegrityTrustRuntime
    fun scheduleIntegrityTrustStartupCheck()
    fun readStartupFailureMessage(): String?
}
