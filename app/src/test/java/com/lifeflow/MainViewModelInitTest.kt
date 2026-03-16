package com.lifeflow

import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelInitTest : MainViewModelTestBase() {

    @Test
    fun `init loads required permissions and keeps init error null`() {
        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = emptySet(),
                stepsValue = 0L,
                avgHeartRateValue = null
            )
        )

        try {
            settleMain()

            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.requiredHealthPermissions.value
            )
            assertNull(viewModel.healthPermissionsInitError.value)
            assertTrue(viewModel.uiState.value is UiState.Loading)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `init keeps protected data hidden before authentication even when repository has granted permissions and metrics`() {
        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 4321L,
                avgHeartRateValue = 67.4
            )
        )

        try {
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Loading)
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.requiredHealthPermissions.value
            )
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertNull(viewModel.digitalTwinState.value)
            assertNull(viewModel.healthPermissionsInitError.value)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `init stores permissions init error when required permissions lookup fails`() {
        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = emptySet(),
                stepsValue = 0L,
                avgHeartRateValue = null,
                throwsOnRequiredPermissions = true
            )
        )

        try {
            settleMain()

            assertTrue(viewModel.requiredHealthPermissions.value.isEmpty())
            assertNotNull(viewModel.healthPermissionsInitError.value)
            assertTrue(
                viewModel.healthPermissionsInitError.value!!.contains("IllegalStateException")
            )
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }
}