package com.lifeflow.domain.security

/**
 * Domain-safe snapshot of the trusted base runtime envelope.
 */
data class TrustedBaseSnapshot(
    val mode: TrustedBaseMode,
    val trustState: TrustState,
    val emergencyWindow: EmergencyAccessWindow? = null
) {
    init {
        require(
            !(mode == TrustedBaseMode.BREAK_GLASS_WINDOW && emergencyWindow == null)
        ) {
            "BREAK_GLASS_WINDOW mode requires emergencyWindow."
        }

        require(
            !(mode != TrustedBaseMode.BREAK_GLASS_WINDOW && emergencyWindow != null)
        ) {
            "Emergency window is only valid in BREAK_GLASS_WINDOW mode."
        }
    }

    val isTrustedBaseRestricted: Boolean
        get() = mode != TrustedBaseMode.NORMAL
}
