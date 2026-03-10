package com.lifeflow.data.wellbeing

import android.content.Context
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthConnectManagerInstrumentedTest {

    private val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @Test
    fun permissionsReturnsExpectedReadPermissions() {
        val permissions = HealthConnectManager.permissions()

        assertEquals(
            setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(HeartRateRecord::class)
            ),
            permissions
        )
    }

    @Test
    fun getSdkStatusReturnsKnownEnumWithoutThrowing() {
        val status = HealthConnectManager.getSdkStatus(appContext)

        assertTrue(
            status == HealthConnectManager.SdkStatus.Available ||
                status == HealthConnectManager.SdkStatus.NotInstalled ||
                status == HealthConnectManager.SdkStatus.NotSupported ||
                status == HealthConnectManager.SdkStatus.UpdateRequired
        )
    }
}