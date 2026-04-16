package com.lifeflow.security

import android.content.Context

internal object EmergencyAuthorityBoundaryBootstrap {

    fun start(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): EmergencyAuthorityBoundaryHandle {
        val runtime = if (isInstrumentation) {
            EmergencyAuthorityRuntime(
                emergencyAuditSink = LocalEmergencyAuditSink,
                emergencyArtifactRegistry = LocalEmergencyArtifactRegistry,
                transport = null
            )
        } else {
            val transport = GrpcEmergencyAuthorityTransport.create(applicationContext)
            EmergencyAuthorityRuntime(
                emergencyAuditSink = ExternalEmergencyAuditSink(transport),
                emergencyArtifactRegistry = ExternalEmergencyArtifactRegistry(transport),
                transport = transport
            )
        }

        SecurityEmergencyAccessAuthority.initialize(
            emergencyAuditSink = runtime.emergencyAuditSink,
            emergencyArtifactRegistry = runtime.emergencyArtifactRegistry
        )

        return runtime
    }
}
