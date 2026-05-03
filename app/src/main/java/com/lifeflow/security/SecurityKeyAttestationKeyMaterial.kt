package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.spec.ECGenParameterSpec

internal fun generateAttestedKeyPair(
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

internal fun loadAttestationCertificateChain(): Array<Certificate> {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null)
    return keyStore.getCertificateChain(ATTESTATION_KEY_ALIAS) ?: emptyArray()
}

internal fun deleteExistingAttestationAliasIfPresent() {
    try {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (keyStore.containsAlias(ATTESTATION_KEY_ALIAS)) {
            keyStore.deleteEntry(ATTESTATION_KEY_ALIAS)
        }
    } catch (exception: Exception) {
        Log.w(
            TAG,
            "Existing attestation alias cleanup failed.",
            exception
        )
    }
}

private const val TAG = "SecurityKeyAttestation"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val EC_CURVE = "secp256r1"
