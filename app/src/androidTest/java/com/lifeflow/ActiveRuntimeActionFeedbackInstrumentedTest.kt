package com.lifeflow

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActiveRuntimeActionFeedbackInstrumentedTest {

    @Test
    fun refreshWithUiFeedback_success_callsRefreshAndReportsRequestMessage() {
        val viewModel = RecordingActiveRuntimeViewModel()
        var message: String? = null

        requestActiveRuntimeRefreshWithUiFeedback(
            viewModel = viewModel,
            requestMessage = "Manual refresh requested",
            setLastAction = { message = it }
        )

        assertEquals(1, viewModel.refreshCalls)
        assertEquals("Manual refresh requested", message)
    }

    @Test
    fun refreshWithUiFeedback_failure_reportsStableFailureMessage() {
        val viewModel = RecordingActiveRuntimeViewModel(
            refreshFailure = IllegalStateException("refresh denied")
        )
        var message: String? = null

        requestActiveRuntimeRefreshWithUiFeedback(
            viewModel = viewModel,
            requestMessage = "unused",
            setLastAction = { message = it }
        )

        assertEquals(1, viewModel.refreshCalls)
        assertEquals(
            "Refresh trigger failed: IllegalStateException: refresh denied",
            message
        )
    }

    @Test
    fun openHealthConnectSettings_primarySuccess_marksSettingsOpened() {
        val actions = mutableListOf<String?>()
        var opened = 0
        var failed = 0
        var message: String? = null

        openActiveRuntimeHealthConnectSettingsWithFallback(
            appPackageName = "com.lifeflow.debug",
            onStartIntent = { intent -> actions += intent.action },
            onSettingsOpened = { opened++ },
            onSettingsOpenFailed = { failed++ },
            setLastAction = { message = it }
        )

        assertEquals(listOf(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS), actions)
        assertEquals(1, opened)
        assertEquals(0, failed)
        assertEquals("Opened Health Connect settings", message)
    }

    @Test
    fun openHealthConnectSettings_primaryFailure_fallsBackToAppSettings() {
        val actions = mutableListOf<String?>()
        val data = mutableListOf<String?>()
        var callCount = 0
        var opened = 0
        var failed = 0
        var message: String? = null

        openActiveRuntimeHealthConnectSettingsWithFallback(
            appPackageName = "com.lifeflow.debug",
            onStartIntent = { intent ->
                callCount++
                actions += intent.action
                data += intent.data?.toString()
                if (callCount == 1) {
                    throw IllegalStateException("primary unavailable")
                }
            },
            onSettingsOpened = { opened++ },
            onSettingsOpenFailed = { failed++ },
            setLastAction = { message = it }
        )

        assertEquals(2, callCount)
        assertEquals(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS, actions[0])
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, actions[1])
        assertEquals("package:com.lifeflow.debug", data[1])
        assertEquals(1, opened)
        assertEquals(0, failed)
        assertEquals(
            "HC settings unavailable (IllegalStateException). Opened App settings instead.",
            message
        )
    }

    @Test
    fun openHealthConnectSettings_whenBothIntentsFail_marksOpenFailed() {
        var callCount = 0
        var opened = 0
        var failed = 0
        var message: String? = null

        openActiveRuntimeHealthConnectSettingsWithFallback(
            appPackageName = "com.lifeflow.debug",
            onStartIntent = {
                callCount++
                if (callCount == 1) {
                    throw IllegalStateException("primary unavailable")
                } else {
                    throw UnsupportedOperationException("fallback unavailable")
                }
            },
            onSettingsOpened = { opened++ },
            onSettingsOpenFailed = { failed++ },
            setLastAction = { message = it }
        )

        assertEquals(2, callCount)
        assertEquals(0, opened)
        assertEquals(1, failed)
        assertEquals(
            "Unable to open settings: IllegalStateException / UnsupportedOperationException",
            message
        )
    }

    private class RecordingActiveRuntimeViewModel(
        private val refreshFailure: Exception? = null
    ) : ActiveRuntimeViewModelContract {
        override val uiState: State<UiState> = mutableStateOf(UiState.Loading)
        override val lastAction: State<String> = mutableStateOf("")
        override val freeTierMessage: State<String> = mutableStateOf("")
        override val healthConnectState: State<HealthConnectUiState> =
            unusedState("healthConnectState")
        override val requiredHealthPermissions: State<Set<String>> =
            mutableStateOf(emptySet())
        override val grantedHealthPermissions: State<Set<String>> =
            mutableStateOf(emptySet())
        override val healthPermissionsInitError: State<String?> =
            mutableStateOf(null)
        override val digitalTwinState: State<DigitalTwinState?> =
            mutableStateOf(null)
        override val wellbeingAssessment: State<WellbeingAssessment?> =
            mutableStateOf(null)
        override val boundarySnapshot: State<MainBoundarySnapshot> =
            mutableStateOf(MainBoundarySnapshot.initial())
        override val quickCaptureLibrary: State<QuickCaptureLibraryPresentation> =
            mutableStateOf(QuickCaptureLibraryPresentation.initial())

        var refreshCalls = 0

        override fun refreshMetricsAndTwinNow() {
            refreshCalls++
            refreshFailure?.let { throw it }
        }

        override fun saveQuickCaptureDraft(note: String) = Unit
        override fun loadQuickCaptureLibrary() = Unit
        override fun deleteQuickCapture(id: String) = Unit
        override fun onHealthPermissionsResult(granted: Set<String>) = Unit
        override fun onAuthenticationSuccess() = Unit
        override fun onAuthenticationError(message: String) = Unit
        override fun onAppBackgrounded() = Unit
        override fun onAppForegrounded() = Unit
        override fun resetVault() = Unit
        override fun isSessionAuthorizedForUi(): Boolean = false

        private companion object {
            private fun <T> unusedState(name: String): State<T> =
                object : State<T> {
                    override val value: T
                        get() = error("$name is not used in this test")
                }
        }
    }
}
