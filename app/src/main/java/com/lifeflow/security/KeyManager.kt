package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyManager(
    private val alias: String = "LifeFlow_Master_Key",
    private val requireUserAuth: Boolean = true
) {

    init {
        require(alias.isNotBlank()) { "Key alias must not be blank" }
    }

    private companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_SIZE_BITS = 256
        private const val AUTH_VALIDITY_SECONDS = 30
    }

    @Synchronized
    fun generateKey() {
        if (keyExists()) return

        // 1) Prefer StrongBox (if supported) with safe fallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateKeyInternal(useStrongBox = true)
                return
            } catch (_: StrongBoxUnavailableException) {
                // Fallback below
            } catch (_: Exception) {
                // Any unexpected keystore/provider issue -> fallback below
            }
        }

        // 2) Fallback: normal TEE-backed / software-backed depending on device
        generateKeyInternal(useStrongBox = false)
    }

    private fun generateKeyInternal(useStrongBox: Boolean) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)

        // --- User auth requirement (prod default = true; androidTest can disable) ---
        if (requireUserAuth) {
            builder.setUserAuthenticationRequired(true)

            // Invalidate key when new biometric is enrolled (API 24+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }

            // Auth rules differ by API level:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+: biometric strong only, 30s window after auth
                builder.setUserAuthenticationParameters(
                    AUTH_VALIDITY_SECONDS,
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
            } else {
                // API 23–29: cannot strictly force "biometric strong" at keystore level.
                // We enforce biometric in UI (BiometricPrompt), and use a short validity window here.
                @Suppress("DEPRECATION")
                builder.setUserAuthenticationValidityDurationSeconds(AUTH_VALIDITY_SECONDS)
            }
        }

        // StrongBox preference (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && useStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    @Synchronized
    fun getKey(): SecretKey {
        val keyStore = loadKeyStore()
        val key = keyStore.getKey(alias, null)

        if (key == null) {
            // Self-heal: create key if missing (first install / cleared keystore)
            generateKey()
            val keyAfter = loadKeyStore().getKey(alias, null)
                ?: throw IllegalStateException("Master key not found after generation")
            if (keyAfter !is SecretKey) throw IllegalStateException("Invalid key type")
            return keyAfter
        }

        if (key !is SecretKey) {
            throw IllegalStateException("Invalid key type")
        }

        return key
    }

    @Synchronized
    fun deleteKey() {
        val keyStore = loadKeyStore()
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    @Synchronized
    fun keyExists(): Boolean {
        val keyStore = loadKeyStore()
        return keyStore.containsAlias(alias)
    }

    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }
}