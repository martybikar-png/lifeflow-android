package com.lifeflow

import com.lifeflow.security.SecurityAuthPerUseCryptoProvider

/**
 * Narrow startup/runtime surface for UI and activity entry points.
 *
 * Purpose:
 * - keep UI unaware of concrete app runtime implementation
 * - expose only startup retry, failure reading, and main ViewModel factory access
 */
internal interface StartupRuntimeEntryPoint {
    fun ensureStarted(): Boolean
    fun requireMainViewModelFactory(): MainViewModelFactory
    fun authPerUseCryptoProviderOrNull(): SecurityAuthPerUseCryptoProvider?
    fun readStartupFailureMessage(): String?
}
