package com.lifeflow

import com.lifeflow.security.IntegrityTrustRuntime
import com.lifeflow.security.SecurityAuthPerUseCryptoProvider
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal class LifeFlowAppRuntimeBindings(
    private val appGraph: LifeFlowAppGraph,
    val mainViewModelFactory: MainViewModelFactory,
    val hardeningReport: SecurityHardeningGuard.HardeningReport?,
    val authPerUseCryptoProvider: SecurityAuthPerUseCryptoProvider?,
    val integrityTrustRuntime: IntegrityTrustRuntime
) : AutoCloseable {

    override fun close() {
        appGraph.close()
    }
}
