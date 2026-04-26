package com.lifeflow.security

import android.content.Context
import android.util.Log
import com.lifeflow.BuildConfig
import com.lifeflow.security.integrity.PlayIntegrityBootstrapPreparation

internal object IntegrityTrustBoundaryBootstrap {

    private const val TAG = "IntegrityTrustBootstrap"

    fun start(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): IntegrityTrustRuntime {
        val bootstrapHandle = PlayIntegrityBootstrapPreparation.start(
            applicationContext = applicationContext,
            isInstrumentation = isInstrumentation
        )

        val cloudProjectNumber = if (isInstrumentation) {
            0L
        } else {
            BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER
        }

        val transport = if (isInstrumentation) {
            null
        } else {
            createIntegrityTrustTransportOrNull(
                applicationContext = applicationContext
            )
        }

        return IntegrityTrustRuntime(
            applicationContext = applicationContext.applicationContext,
            cloudProjectNumber = cloudProjectNumber,
            bootstrapHandle = bootstrapHandle,
            transport = transport
        )
    }

    private fun createIntegrityTrustTransportOrNull(
        applicationContext: Context
    ): IntegrityTrustTransport? {
        return try {
            val tlsMaterialProvider = IntegrityTrustTlsMaterialBootstrap.create(
                applicationContext = applicationContext
            )
            GrpcIntegrityTrustTransport.create(
                applicationContext = applicationContext,
                tlsMaterialProvider = tlsMaterialProvider
            )
        } catch (exception: Exception) {
            Log.w(
                TAG,
                "Integrity trust transport disabled: TLS bootstrap is unavailable. External verdict transport remains fail-closed.",
                exception
            )
            null
        }
    }
}
