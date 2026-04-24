package com.lifeflow.security

import android.content.Context
import android.util.Log

internal object EmergencyAuthorityBoundaryBootstrap {

    private const val TAG = "EmergencyAuthorityBootstrap"

    fun start(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): EmergencyAuthorityBoundaryHandle {
        val runtime = if (isInstrumentation) {
            EmergencyAuthorityRuntime(
                emergencyAuditSink = InstrumentationEmergencyAuditSink,
                emergencyArtifactRegistry = InstrumentationEmergencyArtifactRegistry,
                transport = null
            )
        } else {
            val grpcTransport = runCatching {
                GrpcEmergencyAuthorityTransport.create(applicationContext)
            }.onFailure { throwable ->
                Log.w(
                    TAG,
                    "Emergency authority transport disabled: bootstrap failed. Break-glass remains fail-closed.",
                    throwable
                )
            }.getOrNull()

            val authorityTransport: EmergencyAuthorityTransport =
                grpcTransport ?: UnconfiguredEmergencyAuthorityTransport

            EmergencyAuthorityRuntime(
                emergencyAuditSink = ExternalEmergencyAuditSink(authorityTransport),
                emergencyArtifactRegistry = ExternalEmergencyArtifactRegistry(authorityTransport),
                transport = grpcTransport
            )
        }

        SecurityEmergencyAccessAuthority.initialize(
            emergencyAuditSink = runtime.emergencyAuditSink,
            emergencyArtifactRegistry = runtime.emergencyArtifactRegistry
        )

        return runtime
    }
}
