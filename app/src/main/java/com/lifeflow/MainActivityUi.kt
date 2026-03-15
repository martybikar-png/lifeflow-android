package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal fun HandlePendingResumeAction(
    pending: Boolean,
    onConsumePending: () -> Unit,
    onResumeAction: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnConsumePending by rememberUpdatedState(onConsumePending)
    val latestOnResumeAction by rememberUpdatedState(onResumeAction)

    DisposableEffect(lifecycleOwner, pending) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pending) {
                latestOnConsumePending()
                latestOnResumeAction()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
internal fun StartupFailureScreen(
    message: String,
    lastAction: String,
    onRetryStartup: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    ScreenContainer(title = "LifeFlow Startup Error") {
        StartupFailureMessageCard(message = message)

        Spacer(modifier = Modifier.height(16.dp))

        GuidanceCard(
            title = "What to do next",
            message = startupRecoveryGuidance(message)
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(title = "Recovery actions") {
            Text(
                text = startupRecoveryActionHint(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRetryStartup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry startup")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onOpenAppSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open App settings")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LastActionCard(lastAction = lastAction)
    }
}

@Composable
private fun StartupFailureMessageCard(message: String) {
    ActionCard(title = "Application startup failed") {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Startup status",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = startupStatusLabel(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}