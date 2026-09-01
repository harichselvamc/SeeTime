
package com.harichselvamc.seetime.camera_feature.ui

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import androidx.compose.material3.MaterialTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.asExecutor
import com.google.common.util.concurrent.ListenableFuture

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraOverlayScreen(viewModel: TimeViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        if (!cameraPermissionState.hasPermission) {
            cameraPermissionState.launchPermissionRequest()
        }
        onDispose { /* Cleanup if needed */ }
    }

    if (cameraPermissionState.hasPermission) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            lifecycleOwner = lifecycleOwner,
            context = context
        ) {
            // Overlay content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top content
                Column {
                    val currentTime = ZonedDateTime.now()
                    val localTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
                    Text(
                        text = "Live Solar Time: ${currentTime.format(localTimeFormatter)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    // Display remote time from the first time pair if available
                    state.pairs.firstOrNull()?.let { firstPair ->
                        Text(
                            text = "Remote Time (${shortZoneName(firstPair.toZone)}): ${firstPair.displayToTime}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                // Bottom content (e.g., capture button, controls)
                // For now, let's just keep it simple
            }
        }
    } else {
        // Show a message or a button to request permission
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Camera permission required to use this feature.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
            )
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner,
    context: android.content.Context,
    overlayContent: @Composable BoxScope.() -> Unit
) {
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var previewView: PreviewView? by remember { mutableStateOf(null) }

    DisposableEffect(lifecycleOwner, cameraProviderFuture, cameraExecutor) {
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (exc: Exception) {
                println("CameraX binding failed: ${exc.localizedMessage}")
            }
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProviderFuture.removeListener(listener, ContextCompat.getMainExecutor(context))
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also {
                previewView = it
            }
        },
        modifier = modifier
    )

    Box(modifier = modifier) {
        overlayContent()
    }
}

private fun shortZoneName(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')
