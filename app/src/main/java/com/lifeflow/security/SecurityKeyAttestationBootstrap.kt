package com.lifeflow.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.spec.ECGenParameterSpec
import java.util.concurrent.atomic.AtomicReference

internal enum class SecurityKeyAttestationStatus {
    CAPTURED,
    UNAVAILABLE
}

internal enum class SecurityKeyAttestationFailureKind {
    UNAVAILABLE,
    HARD_FAILURE
}

internal data class SecurityKeyAttestationEvidence(
    val status: SecurityKeyAttestationStatus,
    val keyAlias: String,
    val chainEntryCount: Int,
    val challengeBase64: String? = null,
    val certificateChainDerBase64: List<String> = emptyList(),
    val challengeSha256: String? = null,
    val leafCertificateSha256: String? = null,
    val strongBoxRequested: Boolean,
    val failureKind: SecurityKeyAttestationFailureKind? = null,
    val failureReason: String? = null
) {
    init {
        require(keyAlias.isNotBlank()) { "keyAlias must not be blank." }
        require(chainEntryCount >= 0) { "chainEntryCount must be >= 0." }
        certificateChainDerBase64.forEach { entry ->
            require(entry.isNotBlank()) {
                "certificateChainDerBase64 entries must not be blank."
            }
        }

        when (status) {
            SecurityKeyAttestationStatus.CAPTURED -> {
                require(chainEntryCount > 0) {
                    "chainEntryCount must be > 0 for CAPTURED attestation evidence."
                }
                require(!challengeBase64.isNullOrBlank()) {
                    "challengeBase64 is required for CAPTURED attestation evidence."
                }
                require(certificateChainDerBase64.isNotEmpty()) {
                    "certificateChainDerBase64 is required for CAPTURED attestation evidence."
                }
                require(certificateChainDerBase64.size == chainEntryCount) {
                    "chainEntryCount must match certificateChainDerBase64 size for CAPTURED attestation evidence."
                }
                require(!challengeSha256.isNullOrBlank()) {
                    "challengeSha256 is required for CAPTURED attestation evidence."
                }
                require(!leafCertificateSha256.isNullOrBlank()) {
                    "leafCertificateSha256 is required for CAPTURED attestation evidence."
                }
                require(failureKind == null) {
                    "failureKind must be null for CAPTURED attestation evidence."
                }
                require(failureReason == null) {
                    "failureReason must be null for CAPTURED attestation evidence."
                }
            }

            SecurityKeyAttestationStatus.UNAVAILABLE -> {
                require(chainEntryCount == 0) {
                    "chainEntryCount must be 0 for UNAVAILABLE attestation evidence."
                }
                require(certificateChainDerBase64.isEmpty()) {
                    "certificateChainDerBase64 must be empty for UNAVAILABLE attestation evidence."
                }
                require(failureKind != null) {
                    "failureKind is required for UNAVAILABLE attestation evidence."
                }
                require(!failureReason.isNullOrBlank()) {
                    "failureReason is required for UNAVAILABLE attestation evidence."
                }
            }
        }
    }
}

internal object SecurityKeyAttestationRegistry {

    private val currentEvidence = AtomicReference<SecurityKeyAttestationEvidence?>(null)

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

internal object SecurityKeyAttestationBootstrap {

    private const val TAG = "SecurityKeyAttestation"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val EC_CURVE = "secp256r1"
    private const val REQUEST_BOUND_CHALLENGE_CONTEXT =
        "lifeflow-request-bound-attestation-v1"

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

        val evidence = captureEvidence(
            challenge = buildRequestBoundChallenge(requestHash)
        )
        SecurityKeyAttestationRegistry.store(evidence)
        return evidence
    }

    private fun captureEvidence(
        challenge: ByteArray
    ): SecurityKeyAttestationEvidence {
        deleteExistingAliasSilently()

        return try {
            val strongBoxRequested = generateAttestedKeyPair(challenge)
            val certificateChain = loadCertificateChain()

            if (certificateChain.isEmpty()) {
                unavailableEvidence(
                    reason = "Attestation certificate chain is missing.",
                    strongBoxRequested = strongBoxRequested
                )
            } else {
                val encodedChain = certificateChain.map { certificate ->
                    Base64.encodeToString(certificate.encoded, Base64.NO_WRAP)
                }

                SecurityKeyAttestationEvidence(
                    status = SecurityKeyAttestationStatus.CAPTURED,
                    keyAlias = ATTESTATION_KEY_ALIAS,
                    chainEntryCount = encodedChain.size,
                    challengeBase64 = Base64.encodeToString(challenge, Base64.NO_WRAP),
                    certificateChainDerBase64 = encodedChain,
                    challengeSha256 = sha256Base64(challenge),
                    leafCertificateSha256 = sha256Base64(certificateChain.first().encoded),
                    strongBoxRequested = strongBoxRequested
                )
            }
        } catch (throwable: Throwable) {
            deleteExistingAliasSilently()
            hardFailureEvidence(
                reason = "${throwable::class.java.simpleName}: ${throwable.message ?: "unknown"}",
                strongBoxRequested = false
            )
        }
    }

    private fun buildRequestBoundChallenge(
        requestHash: String
    ): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(REQUEST_BOUND_CHALLENGE_CONTEXT.toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(requestHash.trim().toByteArray(Charsets.UTF_8))
        return digest.digest()
    }

    private fun generateAttestedKeyPair(
        challenge: ByteArray
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateAttestedKeyPairInternal(
                    challenge = challenge,
                    useStrongBox = true
                )
                return true
            } catch (_: StrongBoxUnavailableException) {
            }
        }

        generateAttestedKeyPairInternal(
            challenge = challenge,
            useStrongBox = false
        )
        return false
    }

    private fun generateAttestedKeyPairInternal(
        challenge: ByteArray,
        useStrongBox: Boolean
    ) {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            ATTESTATION_KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .setDigests(
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            )
            .setAttestationChallenge(challenge)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && useStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyPairGenerator.initialize(builder.build())
        keyPairGenerator.generateKeyPair()
    }

    private fun loadCertificateChain(): Array<Certificate> {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getCertificateChain(ATTESTATION_KEY_ALIAS) ?: emptyArray()
    }

    private fun deleteExistingAliasSilently() {
        runCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (keyStore.containsAlias(ATTESTATION_KEY_ALIAS)) {
                keyStore.deleteEntry(ATTESTATION_KEY_ALIAS)
            }
        }.onFailure { throwable ->
            Log.w(
                TAG,
                "Existing attestation alias cleanup failed.",
                throwable
            )
        }
    }

    private fun sha256Base64(
        value: ByteArray
    ): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun unavailableEvidence(
        reason: String,
        strongBoxRequested: Boolean
    ): SecurityKeyAttestationEvidence {
        Log.w(TAG, "Key attestation unavailable: $reason")
        return SecurityKeyAttestationEvidence(
            status = SecurityKeyAttestationStatus.UNAVAILABLE,
            keyAlias = ATTESTATION_KEY_ALIAS,
            chainEntryCount = 0,
            strongBoxRequested = strongBoxRequested,
            failureKind = SecurityKeyAttestationFailureKind.UNAVAILABLE,
            failureReason = reason
        )
    }

    private fun hardFailureEvidence(
        reason: String,
        strongBoxRequested: Boolean
    ): SecurityKeyAttestationEvidence {
        Log.e(TAG, "Key attestation hard failure: $reason")
        return SecurityKeyAttestationEvidence(
            status = SecurityKeyAttestationStatus.UNAVAILABLE,
            keyAlias = ATTESTATION_KEY_ALIAS,
            chainEntryCount = 0,
            strongBoxRequested = strongBoxRequested,
            failureKind = SecurityKeyAttestationFailureKind.HARD_FAILURE,
            failureReason = reason
        )
    }
}
