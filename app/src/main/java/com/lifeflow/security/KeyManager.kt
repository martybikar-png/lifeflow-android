package com.lifeflow.security

import android.os.Build
import android.security.keystore.StrongBoxUnavailableException
import java.security.ProviderException
import javax.crypto.SecretKey

class KeyManager(
    private val alias: String = KEY_MANAGER_DEFAULT_ALIAS,
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
        fun supportsAuthPerUseBiometric(): Boolean =
            supportsKeystoreAuthPerUseBiometric()
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
                generateKeystoreKey(
                    alias = alias,
                    authenticationPolicy = authenticationPolicy,
                    useStrongBox = true
                )
                return
            } catch (_: StrongBoxUnavailableException) {
            } catch (_: ProviderException) {
            }
        }

        generateKeystoreKey(
            alias = alias,
            authenticationPolicy = authenticationPolicy,
            useStrongBox = false
        )
    }

    @Synchronized
    fun readKeyPosture(): KeyPostureSnapshot {
        return readKeyPostureSnapshot(alias = alias)
    }

    @Synchronized
    fun requireOperationalKeyPosture(): KeyPostureSnapshot {
        return requireKeystoreOperationalPosture(
            alias = alias,
            authenticationPolicy = authenticationPolicy,
            snapshot = readKeyPosture()
        )
    }

    @Synchronized
    fun getKey(): SecretKey {
        val keyStore = loadAndroidKeyStore(alias = alias)
        val key = loadSecretKeyOrNull(
            alias = alias,
            keyStore = keyStore,
            operation = "get-key"
        )

        if (key == null) {
            generateKey()

            val keyAfter = loadSecretKeyOrNull(
                alias = alias,
                keyStore = loadAndroidKeyStore(alias = alias),
                operation = "get-key-after-generate"
            ) ?: failKeystoreOperation(
                code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
                message = "Master key not found after generation for alias=$alias."
            )

            requireOperationalKeyPosture()
            return keyAfter
        }

        requireOperationalKeyPosture()
        return key
    }

    @Synchronized
    fun deleteKey() {
        val keyStore = loadAndroidKeyStore(alias = alias)
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    @Synchronized
    fun keyExists(): Boolean {
        val keyStore = loadAndroidKeyStore(alias = alias)
        return keyStore.containsAlias(alias)
    }
}