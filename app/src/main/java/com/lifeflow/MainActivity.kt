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
        val app = application as LifeFlowApplication
        val startupBindings = resolveStartupBindings(app)
        setContent {
            LifeFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!startupBindings.startupReady) {
                        StartupFailureContent(
                            app = app,
                            appPackageName = packageName,
                            onStartIntent = { intent -> startActivity(intent) },
                            onRecreateActivity = { recreate() }
                        )
                    } else {
                        MainActivityContent(
                            viewModel = requireNotNull(startupBindings.viewModel),
                            biometricAuthManager = requireNotNull(startupBindings.biometricAuthManager),
                            appPackageName = packageName,
                            onStartIntent = { intent -> startActivity(intent) }
                        )
                    }
                }
            }
        }
    }
}
