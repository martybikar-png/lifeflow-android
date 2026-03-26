package com.lifeflow

import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelWellbeingAssessmentTest : MainViewModelTestBase() {

    @Test
    fun `refresh after auth populates wellbeing assessment`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "wellbeing assessment should be exposed after authenticated refresh"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 4321L,
                avgHeartRateValue = 71.6
            )
        )

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            val wellbeing = viewModel.wellbeingAssessment.value
            assertNotNull(wellbeing)
            assertEquals(ActivityLevel.LOW, wellbeing!!.activityLevel)
            assertEquals(HeartRateStatus.NORMAL, wellbeing.heartRateStatus)
            assertEquals(OverallReadiness.LOW, wellbeing.overallReadiness)
            assertTrue(wellbeing.notes.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `refresh without auth keeps wellbeing assessment hidden`() {
        SecurityAccessSession.clear()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "wellbeing assessment must remain hidden without auth"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 9876L,
                avgHeartRateValue = 63.4
            )
        )

        try {
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Loading)
            assertNull(viewModel.wellbeingAssessment.value)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `permission callback after auth populates wellbeing assessment`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "permission callback should populate wellbeing assessment"
        )

        val wellbeingRepo = FakeWellbeingRepository(
            sdkStatusValue = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = emptySet(),
            stepsValue = 3210L,
            avgHeartRateValue = 66.2
        )

        val viewModel = newViewModel(wellbeingRepo = wellbeingRepo)

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            assertNull(viewModel.wellbeingAssessment.value)

            wellbeingRepo.grantedPermissionsValue = setOf(stepsPermission, heartRatePermission)

            viewModel.onHealthPermissionsResult(setOf(stepsPermission, heartRatePermission))
            settleMain()

            val wellbeing = viewModel.wellbeingAssessment.value
            assertNotNull(wellbeing)
            assertEquals(ActivityLevel.LOW, wellbeing!!.activityLevel)
            assertEquals(HeartRateStatus.NORMAL, wellbeing.heartRateStatus)
            assertEquals(OverallReadiness.LOW, wellbeing.overallReadiness)
            assertTrue(wellbeing.notes.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `foreground refresh updates wellbeing assessment when session remains valid`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "foreground refresh should keep wellbeing assessment current"
        )

        val wellbeingRepo = FakeWellbeingRepository(
            sdkStatusValue = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.6
        )

        val viewModel = newViewModel(wellbeingRepo = wellbeingRepo)

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            val initialWellbeing = viewModel.wellbeingAssessment.value
            assertNotNull(initialWellbeing)
            assertEquals(ActivityLevel.LOW, initialWellbeing!!.activityLevel)
            assertEquals(HeartRateStatus.NORMAL, initialWellbeing.heartRateStatus)
            assertEquals(OverallReadiness.LOW, initialWellbeing.overallReadiness)

            viewModel.onAppBackgrounded()

            wellbeingRepo.stepsValue = 2222L
            wellbeingRepo.avgHeartRateValue = 73.9

            viewModel.onAppForegrounded()
            settleMain()

            val refreshedWellbeing = viewModel.wellbeingAssessment.value
            assertNotNull(refreshedWellbeing)
            assertEquals(ActivityLevel.LOW, refreshedWellbeing!!.activityLevel)
            assertEquals(HeartRateStatus.NORMAL, refreshedWellbeing.heartRateStatus)
            assertEquals(OverallReadiness.LOW, refreshedWellbeing.overallReadiness)
            assertTrue(refreshedWellbeing.notes.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }
}
