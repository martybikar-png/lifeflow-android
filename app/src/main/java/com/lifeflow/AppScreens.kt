package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lifeflow.core.HealthConnectUiState

private val LoadingCardShape = RoundedCornerShape(28.dp)
private val LoadingInnerShape = RoundedCornerShape(22.dp)
private val LoadingPillShape = RoundedCornerShape(18.dp)
private val LoadingCardPadding = 24.dp
private val LoadingCardSpacing = 18.dp

@Composable
fun LoadingScreen(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean,
    lastAction: String,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit,
    debugLines: List<String>
) {
    val currentStateMessage = loadingMessage(
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )
    val currentFocusMessage = loadingGuidanceMessage(
        isAuthenticating = isAuthenticating,
        healthState = healthState,
        requiredCount = requiredCount,
        grantedCount = grantedCount
    )

    ScreenContainer(title = "LifeFlow") {
        LoadingHeroCard()
        ScreenSectionSpacer()
        LoadingStateCard(currentStateMessage = currentStateMessage)
        ScreenSectionSpacer()
        LoadingFocusCard(currentFocusMessage = currentFocusMessage)
        ScreenSectionSpacer()
        HealthSummaryCard(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        )
        ScreenSectionSpacer()
        LoadingActionsCard(
            isAuthenticating = isAuthenticating,
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
fun FreeTierScreen(
    message: String,
    lastAction: String,
    onUpgradeToCore: () -> Unit,
    debugLines: List<String>
) {
    ScreenContainer(title = "LifeFlow Free") {
        LoadingHeader()
        ScreenSectionSpacer()
        GuidanceCard(
            title = "Free mode, explained clearly",
            leadingIconResId = R.drawable.lf_ic_focus,
            message = "This screen explains the current product tier in a calm way. It should feel informative and low-pressure, not like a hard sales wall."
        )
        ScreenSectionSpacer()
        ActionCard(title = "Current tier state") {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenSectionSpacer()
        ActionCard(title = "What Core opens") {
            Text(
                text = "Core opens the Digital Twin, biometric vault, broader module access, and deeper cross-module intelligence.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScreenSectionSpacer()
            Button(
                onClick = onUpgradeToCore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Core")
            }
        }
        ScreenSectionSpacer()
        ActionCard(title = "Current boundary") {
            Text(
                text = "This is a tier-state screen only. It does not define trust-state truth, biometric authority, or protected execution behavior.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
fun ErrorScreen(
    message: String,
    resetRequired: Boolean,
    lastAction: String,
    onRetry: () -> Unit,
    debugLines: List<String>
) {
    val content = resolveErrorScreenContent(
        message = message,
        resetRequired = resetRequired
    )

    ScreenContainer(title = "LifeFlow") {
        LoadingHeader()
        ScreenSectionSpacer()
        GuidanceCard(
            title = content.guidanceTitle,
            leadingIconResId = R.drawable.lf_ic_focus,
            message = content.guidanceMessage
        )
        ScreenSectionSpacer()
        ActionCard(title = "Current security state") {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenSectionSpacer()
        ActionCard(title = "Required next step") {
            Text(
                text = content.nextStepMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScreenSectionSpacer()
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(content.buttonLabel)
            }
        }
        ScreenSectionSpacer()
        ActionCard(title = "Current boundary") {
            Text(
                text = "This screen reflects the active security state and the next safe action. It does not redefine trust truth, recovery authority, or protected access rules in UI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ScreenFooter(lastAction = lastAction, debugLines = debugLines)
    }
}

@Composable
private fun LoadingHeroCard() {
    Card(
        shape = LoadingCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LoadingCardPadding),
            verticalArrangement = Arrangement.spacedBy(LoadingCardSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = LoadingInnerShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lf_ic_focus),
                        contentDescription = "Loading focus",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(10.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Quiet loading",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Preparing the shell calmly",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Text(
                text = "This loading layer keeps the next step clear and proportional while LifeFlow checks access, session state, and health surfaces.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LoadingPill(text = "Access check")
                LoadingPill(text = "Health state")
                LoadingPill(text = "Soft pacing")
            }
        }
    }
}

@Composable
private fun LoadingStateCard(
    currentStateMessage: String
) {
    Card(
        shape = LoadingCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LoadingCardPadding),
            verticalArrangement = Arrangement.spacedBy(LoadingCardSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current state",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = currentStateMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingFocusCard(
    currentFocusMessage: String
) {
    Card(
        shape = LoadingCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LoadingCardPadding),
            verticalArrangement = Arrangement.spacedBy(LoadingCardSpacing)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current focus",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = currentFocusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = LoadingInnerShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Recommended next move",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Authenticate now or review Health access to unlock a fuller first wellbeing snapshot without turning this step into a harsh system wall.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingPill(text: String) {
    Surface(
        shape = LoadingPillShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun LoadingHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.lifeflow_one_icon),
            contentDescription = "LifeFlow One icon",
            modifier = Modifier.size(96.dp)
        )
    }
}
