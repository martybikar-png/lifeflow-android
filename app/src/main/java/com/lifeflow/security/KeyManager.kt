package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

internal class SecurityKeystorePostureException(
    message: String
) : SecurityException(message)

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

    data class KeyPostureSnapshot(
        val alias: String,
        val keyExists: Boolean,
        val securityLevel: Int?,
        val secureHardwareBacked: Boolean,
        val userAuthenticationRequired: Boolean,
        val userAuthenticationType: Int?,
        val userAuthenticationValiditySeconds: Int?,
        val userAuthenticationEnforcedBySecureHardware: Boolean,
        val invalidatedByBiometricEnrollment: Boolean
    )

    init {
        require(alias.isNotBlank()) { "Key alias must not be blank" }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DEFAULT_ALIAS = "LifeFlow_Master_Key"
        private const val KEY_SIZE_BITS = 256
        private const val AUTH_VALIDITY_SECONDS = 30
        private const val AUTH_PER_USE_VALIDITY_SECONDS = -1

        fun supportsAuthPerUseBiometric(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }
    }

    @Synchronized
    fun ensureKey() {
        generateKey()
        requireOperationalKeyPosture()
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
    fun readKeyPosture(): KeyPostureSnapshot {
        val keyStore = loadKeyStore()
        if (!keyStore.containsAlias(alias)) {
            return KeyPostureSnapshot(
                alias = alias,
                keyExists = false,
                securityLevel = null,
                secureHardwareBacked = false,
                userAuthenticationRequired = false,
                userAuthenticationType = null,
                userAuthenticationValiditySeconds = null,
                userAuthenticationEnforcedBySecureHardware = false,
                invalidatedByBiometricEnrollment = false
            )
        }

        val key = keyStore.getKey(alias, null)
            ?: throw IllegalStateException("Keystore alias exists but key is missing for alias=$alias")
        if (key !is SecretKey) {
            throw IllegalStateException("Invalid key type for alias=$alias")
        }

        val keyInfo = readKeyInfo(key)
        val securityLevel = resolveSecurityLevel(keyInfo)

        return KeyPostureSnapshot(
            alias = alias,
            keyExists = true,
            securityLevel = securityLevel,
            secureHardwareBacked = isSecureHardwareBacked(
                keyInfo = keyInfo,
                securityLevel = securityLevel
            ),
            userAuthenticationRequired = keyInfo.isUserAuthenticationRequired,
            userAuthenticationType =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    keyInfo.userAuthenticationType
                } else {
                    null
                },
            userAuthenticationValiditySeconds =
                keyInfo.userAuthenticationValidityDurationSeconds,
            userAuthenticationEnforcedBySecureHardware =
                keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware,
            invalidatedByBiometricEnrollment =
                keyInfo.isInvalidatedByBiometricEnrollment
        )
    }

    @Synchronized
    fun requireOperationalKeyPosture(): KeyPostureSnapshot {
        val snapshot = readKeyPosture()

        if (!snapshot.keyExists) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: keystore key is missing."
            )
        }

        when (authenticationPolicy) {
            AuthenticationPolicy.NONE -> requireNoAuthenticationPosture(snapshot)
            AuthenticationPolicy.BIOMETRIC_TIME_BOUND -> requireBiometricTimeBoundPosture(snapshot)
            AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE -> requireBiometricAuthPerUsePosture(snapshot)
        }

        return snapshot
    }

    private fun requireNoAuthenticationPosture(
        snapshot: KeyPostureSnapshot
    ) {
        if (snapshot.userAuthenticationRequired) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: authentication must not be required."
            )
        }
    }

    private fun requireBiometricTimeBoundPosture(
        snapshot: KeyPostureSnapshot
    ) {
        if (!snapshot.secureHardwareBacked) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must be hardware-backed."
            )
        }
        if (!snapshot.userAuthenticationRequired) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must require authentication."
            )
        }
        if (!snapshot.userAuthenticationEnforcedBySecureHardware) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound auth must be enforced by secure hardware."
            )
        }
        if (!snapshot.invalidatedByBiometricEnrollment) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must invalidate on biometric enrollment change."
            )
        }
        if ((snapshot.userAuthenticationValiditySeconds ?: 0) <= 0) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must have a positive auth window."
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val authType = snapshot.userAuthenticationType ?: 0
            if (authType and KeyProperties.AUTH_BIOMETRIC_STRONG == 0) {
                failKeyPosture(
                    "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must require BIOMETRIC_STRONG."
                )
            }
        }
    }

    private fun requireBiometricAuthPerUsePosture(
        snapshot: KeyPostureSnapshot
    ) {
        if (!supportsAuthPerUseBiometric()) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric auth-per-use requires Android 11+."
            )
        }
        if (!snapshot.secureHardwareBacked) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must be hardware-backed."
            )
        }
        if (!snapshot.userAuthenticationRequired) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must require authentication."
            )
        }
        if (!snapshot.userAuthenticationEnforcedBySecureHardware) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: auth-per-use auth must be enforced by secure hardware."
            )
        }
        if (!snapshot.invalidatedByBiometricEnrollment) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must invalidate on biometric enrollment change."
            )
        }

        val authType = snapshot.userAuthenticationType ?: 0
        if (authType and KeyProperties.AUTH_BIOMETRIC_STRONG == 0) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must require BIOMETRIC_STRONG."
            )
        }
        if (snapshot.userAuthenticationValiditySeconds != AUTH_PER_USE_VALIDITY_SECONDS) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must require authentication for every use."
            )
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
            requireOperationalKeyPosture()
            return keyAfter
        }

        if (key !is SecretKey) {
            throw IllegalStateException("Invalid key type")
        }

        requireOperationalKeyPosture()
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

    private fun readKeyInfo(
        secretKey: SecretKey
    ): KeyInfo {
        val secretKeyFactory = SecretKeyFactory.getInstance(
            secretKey.algorithm,
            ANDROID_KEYSTORE
        )
        return secretKeyFactory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
    }

    private fun resolveSecurityLevel(
        keyInfo: KeyInfo
    ): Int? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            keyInfo.securityLevel
        } else {
            null
        }
    }

    private fun isSecureHardwareBacked(
        keyInfo: KeyInfo,
        securityLevel: Int?
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && securityLevel != null) {
            securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
                securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX ||
                securityLevel == KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE
        } else {
            @Suppress("DEPRECATION")
            keyInfo.isInsideSecureHardware
        }
    }

    private fun failKeyPosture(
        message: String
    ): Nothing {
        throw SecurityKeystorePostureException(message)
    }

    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }
}
