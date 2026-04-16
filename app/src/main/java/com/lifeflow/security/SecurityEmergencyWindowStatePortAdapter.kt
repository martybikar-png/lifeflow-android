package com.lifeflow.security

/**
 * Narrow read-only emergency window state surface for non-security callers.
 *
 * Purpose:
 * - keep callers out of SecurityEmergencyAccessAuthority internals
 * - expose only the trusted-base window presence needed for gating
 */
internal object SecurityEmergencyWindowStatePortAdapter {

    fun hasActiveTrustedBaseWindow(): Boolean {
        return SecurityEmergencyAccessAuthority.currentWindow() != null
    }
}
