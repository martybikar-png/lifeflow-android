package com.lifeflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.lifeflow.ui.theme.LifeFlowTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyWindowSecurityHardening()

        val startupRuntimeEntryPoint = requireStartupRuntimeEntryPoint()
        val startupBindings = resolveStartupBindings(startupRuntimeEntryPoint)
        val onboardingStore = OnboardingStateStore(this)

        setContent {
            LifeFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppEntry(
                        startupRuntimeEntryPoint = startupRuntimeEntryPoint,
                        startupBindings = startupBindings,
                        initialOnboardingCompleted = onboardingStore.isCompleted(),
                        onMarkOnboardingCompleted = {
                            onboardingStore.setCompleted()
                        },
                        appPackageName = packageName,
                        onStartIntent = { intent -> startActivity(intent) },
                        onRecreateActivity = { recreate() }
                    )
                }
            }
        }
    }
}
