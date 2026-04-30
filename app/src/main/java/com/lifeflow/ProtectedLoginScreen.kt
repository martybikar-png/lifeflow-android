package com.lifeflow

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.lifeflow.core.HealthConnectUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun ProtectedLoginScreen(
    isAuthenticating: Boolean,
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    onAuthenticate: () -> Unit,
    onGrantHealthPermissions: () -> Unit,
    onOpenHealthConnectSettings: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val loginPhotoStore = remember(context) {
        LoginPhotoStore(context)
    }
    val scope = rememberCoroutineScope()

    var loginPhotoVersion by remember(loginPhotoStore) {
        mutableStateOf(loginPhotoStore.loginPhotoVersion())
    }
    var pendingPhotoUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var isSavingPhoto by remember {
        mutableStateOf(false)
    }

    val loginPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        pendingPhotoUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PremiumLoginBlueTop,
                        PremiumLoginBlueBottom
                    )
                )
            )
    ) {
        ProtectedAccessLoginCard(
            modifier = Modifier.fillMaxSize(),
            isAuthenticating = isAuthenticating,
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            loginPhotoVersion = loginPhotoVersion,
            onPickLoginPhoto = {
                loginPhotoPicker.launch(arrayOf("image/*"))
            },
            onAuthenticate = onAuthenticate,
            onGrantHealthPermissions = onGrantHealthPermissions,
            onOpenHealthConnectSettings = onOpenHealthConnectSettings
        )

        val photoUri = pendingPhotoUri
        if (photoUri != null) {
            LoginPhotoCropEditor(
                sourceUri = photoUri,
                isSaving = isSavingPhoto,
                onConfirm = { transform ->
                    if (!isSavingPhoto) {
                        isSavingPhoto = true
                        scope.launch {
                            loginPhotoVersion = withContext(Dispatchers.IO) {
                                loginPhotoStore.saveCroppedLoginPhoto(
                                    sourceUri = photoUri,
                                    transform = transform
                                )
                            }
                            pendingPhotoUri = null
                            isSavingPhoto = false
                        }
                    }
                },
                onCancel = {
                    pendingPhotoUri = null
                    isSavingPhoto = false
                }
            )
        }
    }
}
