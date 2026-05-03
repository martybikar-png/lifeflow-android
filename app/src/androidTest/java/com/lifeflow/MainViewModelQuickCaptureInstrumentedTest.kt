package com.lifeflow

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.SecurityVaultResetAuthorization
import com.lifeflow.security.TrustState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainViewModelQuickCaptureInstrumentedTest {

    private lateinit var context: Context
    private lateinit var runtime: LifeFlowAppRuntime

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearTierTruth(context)
        clearQuickCaptureStores(context)
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "MainViewModelQuickCaptureInstrumentedTest baseline"
        )
        runtime = LifeFlowAppRuntime(applicationContext = context)
    }

    @After
    fun tearDown() {
        if (::runtime.isInitialized) {
            runtime.close()
        }
        clearQuickCaptureStores(context)
        clearTierTruth(context)
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "MainViewModelQuickCaptureInstrumentedTest cleanup"
        )
    }

    @Test
    fun quickCaptureSaveThenLibraryLoad_persistsThroughProtectedRuntime() {
        val viewModel = createProtectedViewModel()

        viewModel.saveQuickCaptureDraft()

        waitUntil("quick capture save") {
            viewModel.lastAction.value == "Quick capture saved."
        }

        viewModel.loadQuickCaptureLibrary()

        waitUntil("capture library load") {
            viewModel.lastAction.value == "Capture library loaded."
        }

        val presentation = viewModel.quickCaptureLibrary.value

        assertTrue(
            presentation.infoBody,
            presentation.infoBody.contains("saved capture")
        )
        assertTrue(
            presentation.infoBody,
            presentation.infoBody.contains("Latest: Quick capture")
        )
        assertTrue(
            presentation.markers.joinToString(),
            presentation.markers.any { it.contains("Saved") }
        )
    }

    private fun createProtectedViewModel(): MainViewModel {
        assertTrue(runtime.ensureStarted())

        val viewModel = runtime
            .requireMainViewModelFactory()
            .create(MainViewModel::class.java)

        prepareVerifiedProtectedSession(viewModel)

        return viewModel
    }

    private fun prepareVerifiedProtectedSession(viewModel: MainViewModel) {
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "MainViewModelQuickCaptureInstrumentedTest verified protected session"
        )
        SecurityAccessSession.grantDefault(context)
        viewModel.uiState.value = UiState.Authenticated

        assertTrue(SecurityAccessSession.isAuthorized())
        assertTrue(viewModel.isSessionAuthorizedForUi())
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
    }

    private fun clearTierTruth(context: Context) {
        TierPreferencesStore(context).clear()
    }

    private fun clearQuickCaptureStores(context: Context) {
        val ok = context
            .getSharedPreferences("lifeflow_diary", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        check(ok) {
            "Failed to clear lifeflow_diary test store."
        }
    }

    private fun waitUntil(
        label: String,
        timeoutMs: Long = 5_000L,
        condition: () -> Boolean
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(50L)
        }

        throw AssertionError("Timed out waiting for $label")
    }

    private fun forceResetSecurityState(
        state: TrustState,
        reason: String
    ) {
        val method = SecurityRuleEngine::class.java.declaredMethods.firstOrNull { candidate ->
            candidate.name.startsWith("forceResetForAdversarialSuite") &&
                candidate.parameterTypes.size == 2 &&
                candidate.parameterTypes[0] == TrustState::class.java &&
                candidate.parameterTypes[1] == String::class.java
        } ?: throw AssertionError(
            buildString {
                append("Could not find compatible forceResetForAdversarialSuite method on SecurityRuleEngine. Available methods: ")
                append(SecurityRuleEngine::class.java.declaredMethods.joinToString { it.name })
            }
        )

        method.isAccessible = true
        method.invoke(SecurityRuleEngine, state, reason)
    }
}
