package com.lifeflow.security

import android.content.Context

internal object SecurityKeyAttestationBootstrap {
    private val evidenceCapturer = SecurityKeyAttestationEvidenceCapturer()

    fun start(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): SecurityKeyAttestationRuntime {
        applicationContext
        isInstrumentation
        SecurityKeyAttestationRegistry.clear()
        return SecurityKeyAttestationRuntime()
    }

    fun captureRequestBoundEvidence(
        requestHash: String
    ): SecurityKeyAttestationEvidence {
        require(requestHash.isNotBlank()) { "requestHash must not be blank." }

        val evidence = evidenceCapturer.captureRequestBoundEvidence(requestHash)
        SecurityKeyAttestationRegistry.store(evidence)
        return evidence
    }
}