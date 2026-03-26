package com.lifeflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal fun HandleStartupFailurePendingResumeAction(
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
