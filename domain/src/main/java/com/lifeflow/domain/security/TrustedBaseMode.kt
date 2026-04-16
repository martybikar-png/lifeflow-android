package com.lifeflow.domain.security

/**
 * Minimal trusted-base execution mode.
 *
 * This is intentionally separate from TrustState:
 * - TrustState describes trust posture
 * - TrustedBaseMode describes the currently allowed minimal operating envelope
 */
enum class TrustedBaseMode {
    NORMAL,
    SAFE_MINIMAL,
    BREAK_GLASS_WINDOW
}
