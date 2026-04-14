package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyManager(
    private val alias: String = DEFAULT_ALIAS,
    private val authenticationPolicy: AuthenticationPolicy =
        AuthenticationPolicy.BIOMETRIC_TIME_BOUND
) {

    enum class AuthenticationPolicy {
        NONE,
        BIOMETRIC_TIME_BOUND,
        BIOMETRIC_AUTH_PER_USE
    }

    init {
        require(alias.isNotBlank()) { "Key alias must not be blank" }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DEFAULT_ALIAS = "LifeFlow_Master_Key"
        private const val KEY_SIZE_BITS = 256
        private const val AUTH_VALIDITY_SECONDS = 30

        fun supportsAuthPerUseBiometric(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }
    }

    @Synchronized
    fun ensureKey() {
        generateKey()
    }

    fun isAuthPerUse(): Boolean =
        authenticationPolicy == AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE

    fun isTimeBoundBiometric(): Boolean =
        authenticationPolicy == AuthenticationPolicy.BIOMETRIC_TIME_BOUND

    @Synchronized
    fun generateKey() {
        if (keyExists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                generateKeyInternal(useStrongBox = true)
                return
            } catch (_: StrongBoxUnavailableException) {
            } catch (_: Exception) {
            }
        }

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

        configureAuthentication(builder)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && useStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    private fun configureAuthentication(
        builder: KeyGenParameterSpec.Builder
    ) {
        when (authenticationPolicy) {
            AuthenticationPolicy.NONE -> Unit

            AuthenticationPolicy.BIOMETRIC_TIME_BOUND -> {
                builder.setUserAuthenticationRequired(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(true)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder.setUserAuthenticationParameters(
                        AUTH_VALIDITY_SECONDS,
                        KeyProperties.AUTH_BIOMETRIC_STRONG
                    )
                } else {
                    @Suppress("DEPRECATION")
                    builder.setUserAuthenticationValidityDurationSeconds(
                        AUTH_VALIDITY_SECONDS
                    )
                }
            }

            AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE -> {
                require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    "Biometric auth-per-use requires Android 11+ (API 30)."
                }

                builder.setUserAuthenticationRequired(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(true)
                }

                builder.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
            }
        }
    }

    @Synchronized
    fun getKey(): SecretKey {
        val keyStore = loadKeyStore()
        val key = keyStore.getKey(alias, null)

        if (key == null) {
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
