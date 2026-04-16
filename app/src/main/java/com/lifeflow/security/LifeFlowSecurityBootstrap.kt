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
    private const val TEST_KEY_ALIAS = "LifeFlow_Test_Key"
    private const val SESSION_KEY_ALIAS = "LifeFlow_Master_Key"
    private const val AUTH_PER_USE_KEY_ALIAS = "LifeFlow_Master_Key_AuthPerUse"

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

        val androidVault = AndroidDataSovereigntyVault(
            applicationContext,
            cryptoBindings.sessionKeyManager
        )
        androidVault.ensureInitialized()

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

        return LifeFlowSecurityBootstrapResult(
            hardeningReport = hardeningReport,
            integrityTrustRuntime = integrityTrustRuntime,
            androidVault = androidVault,
            identityBlobStore = identityBlobStore,
            encryptedIdentityRepository = encryptedIdentityRepository,
            getActiveIdentityUseCase = getActiveIdentityUseCase,
            saveIdentityUseCase = saveIdentityUseCase,
            resetVaultUseCase = resetVaultUseCase,
            encryptionPort = encryptionPort,
            cryptoBindings = cryptoBindings,
            closeables = listOf(
                emergencyAuthorityBoundaryHandle,
                integrityTrustRuntime
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

        return KeyManager(
            alias = AUTH_PER_USE_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE
        )
    }
}
