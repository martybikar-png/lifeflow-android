package com.lifeflow

import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.security.LifeFlowSecurityBootstrapResult

internal class LifeFlowAppGraph(
    private val securityBootstrap: LifeFlowSecurityBootstrapResult,
    private val wellbeingBindings: LifeFlowWellbeingBindings,
    private val digitalTwinOrchestrator: DigitalTwinOrchestrator,
    private val moduleRepositoryBindings: LifeFlowModuleRepositoryBindings,
    private val mainRuntimeBindings: MainRuntimeBindings
) : AutoCloseable {

    override fun close() {
        securityBootstrap.close()
    }
}
