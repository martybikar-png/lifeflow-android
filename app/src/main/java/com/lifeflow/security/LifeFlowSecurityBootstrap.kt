package com.lifeflow.security

import android.content.Context
import android.util.Log
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.usecase.GetActiveIdentityUseCase
import com.lifeflow.domain.usecase.SaveIdentityUseCase
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal class LifeFlowSecurityBootstrapResult internal constructor(
    val hardeningReport: SecurityHardeningGuard.HardeningReport?,
    val integrityTrustRuntime: IntegrityTrustRuntime,
    val deviceBindingManager: DeviceBindingManager,
    val androidVault: AndroidDataSovereigntyVault,
    val identityBlobStore: EncryptedIdentityBlobStore,
    val encryptedIdentityRepository: EncryptedIdentityRepository,
    val getActiveIdentityUseCase: GetActiveIdentityUseCase,
    val saveIdentityUseCase: SaveIdentityUseCase,
    val resetVaultUseCase: ResetVaultUseCase,
    val encryptionPort: EncryptionPortAdapter,
    val cryptoBindings: SecurityCryptoBindings,
    private val closeables: List<AutoCloseable>
) : AutoCloseable {

    override fun close() {
        closeables
            .asReversed()
            .forEach { closeable ->
                closeable.close()
            }
    }
}

internal object LifeFlowSecurityBootstrap {

    private const val TAG = "LifeFlowSecurityBootstrap"

    fun start(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): LifeFlowSecurityBootstrapResult {
        val hardeningReport = if (isInstrumentation) {
            null
        } else {
            val report = SecurityHardeningGuard.assess(applicationContext)
            if (report.isCritical) {
                throw SecurityException(
                    "Security hardening check failed: ${report.findings.joinToString("; ")}"
                )
            }
            if (report.isDegraded) {
                Log.w(TAG, "Security hardening: degraded environment detected. ${report.findings}")
            }
            report
        }

        val cryptoBindings = createCryptoBindings(isInstrumentation = isInstrumentation)

        val deviceBindingStore = DeviceBindingStore(applicationContext)
        val deviceBindingManager = DeviceBindingManager(
            applicationContext = applicationContext,
            store = deviceBindingStore,
            sessionKeyAlias = if (isInstrumentation) TEST_KEY_ALIAS else SESSION_KEY_ALIAS,
            authPerUseKeyAlias = if (cryptoBindings.authPerUseKeyManager != null) {
                AUTH_PER_USE_KEY_ALIAS
            } else {
                null
            },
            attestationKeyAlias = ATTESTATION_KEY_ALIAS,
            clientAuthKeyAlias = INTEGRITY_CLIENT_AUTH_KEY_ALIAS
        )
        val deviceBindingRuntime = SecurityDeviceBindingRuntime(deviceBindingManager)
        deviceBindingRuntime.ensureRegistered()

        val androidVault = AndroidDataSovereigntyVault(
            applicationContext,
            cryptoBindings.sessionKeyManager
        )

        val identityBlobStore = EncryptedIdentityBlobStore(applicationContext)
        val encryptedIdentityRepository = EncryptedIdentityRepository(
            blobStore = identityBlobStore,
            encryptionService = cryptoBindings.sessionEncryptionService,
            vault = androidVault
        )

        val getActiveIdentityUseCase = GetActiveIdentityUseCase(encryptedIdentityRepository)
        val saveIdentityUseCase = SaveIdentityUseCase(encryptedIdentityRepository)

        val resetVaultUseCase = ResetVaultUseCase(
            blobStore = identityBlobStore,
            vault = androidVault
        )

        val encryptionPort = EncryptionPortAdapter(cryptoBindings.sessionEncryptionService)

        val emergencyAuthorityBoundaryHandle = EmergencyAuthorityBoundaryBootstrap.start(
            applicationContext = applicationContext,
            isInstrumentation = isInstrumentation
        )

        val integrityTrustRuntime = IntegrityTrustBoundaryBootstrap.start(
            applicationContext = applicationContext,
            isInstrumentation = isInstrumentation
        )

        val keyAttestationRuntime = SecurityKeyAttestationBootstrap.start(
            applicationContext = applicationContext,
            isInstrumentation = isInstrumentation
        )

        return LifeFlowSecurityBootstrapResult(
            hardeningReport = hardeningReport,
            integrityTrustRuntime = integrityTrustRuntime,
            deviceBindingManager = deviceBindingManager,
            androidVault = androidVault,
            identityBlobStore = identityBlobStore,
            encryptedIdentityRepository = encryptedIdentityRepository,
            getActiveIdentityUseCase = getActiveIdentityUseCase,
            saveIdentityUseCase = saveIdentityUseCase,
            resetVaultUseCase = resetVaultUseCase,
            encryptionPort = encryptionPort,
            cryptoBindings = cryptoBindings,
            closeables = listOf(
                deviceBindingRuntime,
                emergencyAuthorityBoundaryHandle,
                integrityTrustRuntime,
                keyAttestationRuntime
            )
        )
    }

    private fun createCryptoBindings(
        isInstrumentation: Boolean
    ): SecurityCryptoBindings {
        val sessionKeyManager = if (isInstrumentation) {
            KeyManager(
                alias = TEST_KEY_ALIAS,
                authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
            )
        } else {
            KeyManager(
                alias = SESSION_KEY_ALIAS,
                authenticationPolicy = KeyManager.AuthenticationPolicy.BIOMETRIC_TIME_BOUND
            )
        }

        val sessionEncryptionService = EncryptionService(sessionKeyManager)

        val authPerUseKeyManager = createAuthPerUseKeyManager(
            isInstrumentation = isInstrumentation
        )
        val authPerUseEncryptionService = authPerUseKeyManager?.let(::EncryptionService)

        return SecurityCryptoBindings(
            sessionKeyManager = sessionKeyManager,
            sessionEncryptionService = sessionEncryptionService,
            authPerUseKeyManager = authPerUseKeyManager,
            authPerUseEncryptionService = authPerUseEncryptionService
        )
    }

    private fun createAuthPerUseKeyManager(
        isInstrumentation: Boolean
    ): KeyManager? {
        if (isInstrumentation) return null
        if (!KeyManager.supportsAuthPerUseBiometric()) return null

        val keyManager = KeyManager(
            alias = AUTH_PER_USE_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE
        )

        return runCatching {
            keyManager.ensureKey()
            keyManager
        }.getOrElse { throwable ->
            handleAuthPerUseBootstrapFailure(
                keyManager = keyManager,
                throwable = throwable
            )
            null
        }
    }

    private fun handleAuthPerUseBootstrapFailure(
        keyManager: KeyManager,
        throwable: Throwable
    ) {
        when (throwable) {
            is SecurityKeystorePostureException -> {
                deleteAuthPerUseKeySilently(
                    keyManager = keyManager,
                    reason = "posture mismatch"
                )
                Log.w(
                    TAG,
                    "Auth-per-use crypto disabled: keystore posture mismatch.",
                    throwable
                )
            }

            is SecurityKeystoreOperationException -> {
                if (throwable.code == SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE ||
                    throwable.code == SecurityKeystoreFailureCode.KEY_UNRECOVERABLE
                ) {
                    deleteAuthPerUseKeySilently(
                        keyManager = keyManager,
                        reason = "invalid or unrecoverable key"
                    )
                }

                Log.w(
                    TAG,
                    "Auth-per-use crypto disabled: keystore bootstrap failed (${throwable.code}).",
                    throwable
                )
            }

            else -> {
                Log.w(
                    TAG,
                    "Auth-per-use crypto disabled: unexpected bootstrap failure.",
                    throwable
                )
            }
        }
    }

    private fun deleteAuthPerUseKeySilently(
        keyManager: KeyManager,
        reason: String
    ) {
        runCatching {
            keyManager.deleteKey()
        }.onFailure { deleteFailure ->
            Log.w(
                TAG,
                "Auth-per-use key cleanup failed after $reason.",
                deleteFailure
            )
        }
    }
}
