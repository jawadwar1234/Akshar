package com.akshar.dyslexiareader.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.akshar.dyslexiareader.R
import com.akshar.dyslexiareader.ui.theme.CalmTeal
import com.akshar.dyslexiareader.ui.theme.CharcoalTextPrimary
import com.akshar.dyslexiareader.ui.theme.CharcoalTextSecondary
import com.akshar.dyslexiareader.ui.theme.DesignTokens
import com.akshar.dyslexiareader.ui.theme.PaperCreamBackground
import com.akshar.dyslexiareader.ui.theme.PaperCreamBorder
import com.akshar.dyslexiareader.ui.theme.PaperCreamSurface
import com.akshar.dyslexiareader.ui.theme.TerracottaAccent
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraCaptureScreen(
    viewModel: CameraViewModel,
    onImageReadyForCrop: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageCaptured(it)
            onImageReadyForCrop()
        }
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(uiState.isTorchOn) {
        cameraInstance?.cameraControl?.enableTorch(uiState.isTorchOn)
    }

    if (!hasCameraPermission) {
        // Permission Request UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PaperCreamBackground)
                .padding(DesignTokens.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = TerracottaAccent,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
            Text(
                text = stringResource(R.string.camera_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = CharcoalTextPrimary
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            Text(
                text = stringResource(R.string.camera_permission_required),
                style = MaterialTheme.typography.bodyLarge,
                color = CharcoalTextSecondary
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
            ) {
                Text(
                    text = stringResource(R.string.camera_btn_grant),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    // Camera Active Screen
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(Runnable {
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        cameraInstance = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        // Handle failure
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder framing guide
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 96.dp)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.large
                )
        )

        // Top Controls: Hint & Flash Toggle
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(horizontal = DesignTokens.Spacing.lg, vertical = DesignTokens.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.camera_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Flash Toggle with visible label
                OutlinedButton(
                    onClick = viewModel::toggleTorch,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = if (uiState.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = if (uiState.isTorchOn) stringResource(R.string.camera_btn_flash_off) else stringResource(R.string.camera_btn_flash_on),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
            Text(
                text = stringResource(R.string.camera_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        // Bottom Capture Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(DesignTokens.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Import from Gallery
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text("Gallery", style = MaterialTheme.typography.labelMedium)
            }

            // Capture Shutter Button
            Button(
                onClick = {
                    val photoFile = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                ContextCompat.getMainExecutor(context).execute {
                                    viewModel.onImageCaptured(savedUri)
                                    onImageReadyForCrop()
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                // Error handled gracefully
                            }
                        }
                    )
                },
                modifier = Modifier.height(DesignTokens.buttonHeight),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.sm))
                Text(
                    text = stringResource(R.string.camera_btn_capture),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
