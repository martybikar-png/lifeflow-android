package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest

/**
 * Narrow read-only emergency window state surface for non-security callers.
 *
 * Purpose:
 * - keep callers out of SecurityEmergencyAccessAuthority internals
 * - expose only the trusted-base window facts needed for gating
 */
internal object SecurityEmergencyWindowStatePortAdapter {

    fun hasActiveTrustedBaseWindow(): Boolean {
        return SecurityEmergencyAccessAuthority.currentWindow() != null
    }

    fun activeTrustedBaseReadOnlyRequestOrNull(): EmergencyAccessRequest? {
        val window = SecurityEmergencyAccessAuthority.currentWindow()
            ?: return null

        if (!window.trustedBaseOnly) {
            return null
        }

        val now = System.currentTimeMillis()
        val remainingDurationMs = (window.expiresAtEpochMs - now)
            .coerceAtLeast(1L)

        return EmergencyAccessRequest(
            reason = window.reason,
            requestedAtEpochMs = now,
            requestedDurationMs = remainingDurationMs
        )
    }
}
