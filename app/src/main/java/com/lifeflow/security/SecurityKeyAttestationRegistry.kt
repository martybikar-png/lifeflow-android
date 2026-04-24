package com.lifeflow.security

import java.util.concurrent.atomic.AtomicReference

internal object SecurityKeyAttestationRegistry {
    private val currentEvidence =
        AtomicReference<SecurityKeyAttestationEvidence?>(null)

    fun currentOrNull(): SecurityKeyAttestationEvidence? =
        currentEvidence.get()

    internal fun store(
        evidence: SecurityKeyAttestationEvidence
    ) {
        currentEvidence.set(evidence)
    }

    internal fun clear() {
        currentEvidence.set(null)
    }
}

internal class SecurityKeyAttestationRuntime internal constructor() : AutoCloseable {
    override fun close() {
        SecurityKeyAttestationRegistry.clear()
    }
}