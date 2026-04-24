package com.lifeflow.security

import java.util.concurrent.atomic.AtomicReference

internal object SecurityDeviceBindingRegistry {
    private val managerRef = AtomicReference<DeviceBindingManager?>(null)

    fun bind(
        manager: DeviceBindingManager
    ) {
        managerRef.set(manager)
    }

    fun currentOrNull(): DeviceBindingManager? =
        managerRef.get()

    fun clear() {
        managerRef.set(null)
    }
}

internal class SecurityDeviceBindingRuntime internal constructor(
    private val manager: DeviceBindingManager
) : AutoCloseable {

    init {
        SecurityDeviceBindingRegistry.bind(manager)
    }

    fun ensureRegistered(): DeviceBindingSnapshot =
        manager.ensureRegistered()

    override fun close() {
        SecurityDeviceBindingRegistry.clear()
    }
}
