package com.lifeflow.security

import android.content.Context
import com.lifeflow.BuildConfig
import com.lifeflow.security.integrity.PlayIntegrityBootstrapPreparation

internal object IntegrityTrustBoundaryBootstrap {

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
            GrpcIntegrityTrustTransport.create(applicationContext)
        }

        return IntegrityTrustRuntime(
            applicationContext = applicationContext.applicationContext,
            cloudProjectNumber = cloudProjectNumber,
            bootstrapHandle = bootstrapHandle,
            transport = transport
        )
    }
}
