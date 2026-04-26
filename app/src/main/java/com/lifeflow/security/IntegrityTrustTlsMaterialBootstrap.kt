package com.lifeflow.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

internal object IntegrityTrustTlsMaterialBootstrap {
    private const val TAG = "IntegrityTrustBootstrap"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val EC_CURVE = "secp256r1"
    private const val CLIENT_AUTH_CHALLENGE_CONTEXT =
        "lifeflow-integrity-client-auth-v1"

    fun create(
        applicationContext: Context
    ): IntegrityTrustTlsMaterialProvider {
        ensureClientAuthKeyMaterial(
            applicationContext = applicationContext.applicationContext
        )

        return AndroidKeystoreIntegrityTrustTlsMaterialProvider(
            clientAuthKeyAlias = INTEGRITY_CLIENT_AUTH_KEY_ALIAS
        )
    }

    private fun ensureClientAuthKeyMaterial(
        applicationContext: Context
    ) {
        if (hasUsableClientAuthEntry()) {
            return
        }

        deleteClientAuthKeyIfPresent()
        generateClientAuthKeyPair(applicationContext)
    }

    private fun hasUsableClientAuthEntry(): Boolean {
        val keyStore = loadAndroidKeyStore()
        if (!keyStore.containsAlias(INTEGRITY_CLIENT_AUTH_KEY_ALIAS)) {
            return false
        }

        val entry = keyStore.getEntry(INTEGRITY_CLIENT_AUTH_KEY_ALIAS, null)
        val privateKeyEntry = entry as? KeyStore.PrivateKeyEntry ?: return false
        return privateKeyEntry.certificateChain?.isNotEmpty() == true
    }

    private fun generateClientAuthKeyPair(
        applicationContext: Context
    ) {
        val challenge = buildClientAuthChallenge(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateClientAuthKeyPairInternal(
                    challenge = challenge,
                    useStrongBox = true
                )
                return
            } catch (_: StrongBoxUnavailableException) {
            }
        }

        generateClientAuthKeyPairInternal(
            challenge = challenge,
            useStrongBox = false
        )
    }

    private fun generateClientAuthKeyPairInternal(
        challenge: ByteArray,
        useStrongBox: Boolean
    ) {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            INTEGRITY_CLIENT_AUTH_KEY_ALIAS,
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

    private fun buildClientAuthChallenge(
        applicationContext: Context
    ): ByteArray {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(CLIENT_AUTH_CHALLENGE_CONTEXT.toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(applicationContext.packageName.toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(randomBytes)
        return digest.digest()
    }

    private fun deleteClientAuthKeyIfPresent() {
        try {
            val keyStore = loadAndroidKeyStore()
            if (keyStore.containsAlias(INTEGRITY_CLIENT_AUTH_KEY_ALIAS)) {
                keyStore.deleteEntry(INTEGRITY_CLIENT_AUTH_KEY_ALIAS)
            }
        } catch (exception: Exception) {
            Log.w(
                TAG,
                "Integrity client-auth key cleanup failed.",
                exception
            )
        }
    }

    private fun loadAndroidKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }
}
