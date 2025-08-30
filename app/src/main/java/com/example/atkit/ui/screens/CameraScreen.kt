package com.example.atkit.ui.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.atkit.ui.SessionViewModel
import com.example.atkit.utils.FileManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    sessionId: String,
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val capturedImageCount by sessionViewModel.capturedImageCount.collectAsState()

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // ✅ Fixed camera provider initialization
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            try {
                val provider = suspendCoroutine<ProcessCameraProvider> { continuation ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        continuation.resume(cameraProviderFuture.get())
                    }, ContextCompat.getMainExecutor(context))
                }
                cameraProvider = provider
                cameraReady = true
                Log.d("CameraScreen", "Camera provider initialized successfully")
            } catch (e: Exception) {
                Log.e("CameraScreen", "Failed to initialize camera provider", e)
            }
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Session: $sessionId", fontWeight = FontWeight.Bold)
                            Text(
                                "$capturedImageCount images captured",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { navController.navigate("session_end/$sessionId") }
                        ) {
                            Text("End Session", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (cameraReady && cameraProvider != null) {
                    // ✅ Fixed Camera Preview
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                // ✅ Critical fixes for black screen
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        },
                        update = { previewView ->
                            // ✅ Setup camera when view updates
                            cameraProvider?.let { provider ->
                                setupCamera(ctx = context, previewView, lifecycleOwner, provider) { capture ->
                                    imageCapture = capture
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // ✅ Loading state while camera initializes
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Initializing Camera...")
                        }
                    }
                }

                // Camera Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(50.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(64.dp))

                    // Capture Button
                    Button(
                        onClick = {
                            if (!isCapturing && imageCapture != null) {
                                captureImage(
                                    context = context,
                                    imageCapture = imageCapture,
                                    sessionId = sessionId,
                                    onImageCaptured = {
                                        sessionViewModel.incrementImageCount()
                                        isCapturing = false
                                    },
                                    onError = { isCapturing = false }
                                )
                                isCapturing = true
                            }
                        },
                        enabled = !isCapturing && cameraReady && imageCapture != null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Capture",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Image count display
                    Card(
                        modifier = Modifier.padding(start = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "$capturedImageCount",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        // Permission not granted
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Camera,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Please grant camera permission to capture images",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { cameraPermissionState.launchPermissionRequest() }
            ) {
                Text("Grant Permission")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

// ✅ Updated setupCamera function
private fun setupCamera(
    ctx: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    try {
        // ✅ Unbind all use cases before rebinding
        cameraProvider.unbindAll()

        // ✅ Create preview with proper configuration
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // ✅ Create image capture with optimized settings
        val imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        onImageCaptureReady(imageCapture)

        // ✅ Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // ✅ Bind use cases to camera with error handling
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture
        )

        Log.d("CameraScreen", "Camera bound successfully")
        Log.d("CameraScreen", "Camera info: ${camera.cameraInfo}")

    } catch (exc: Exception) {
        Log.e("CameraScreen", "Use case binding failed", exc)
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture?,
    sessionId: String,
    onImageCaptured: () -> Unit,
    onError: () -> Unit
) {
    imageCapture?.let { capture ->
        val photoFile = FileManager.createImageFile(context, sessionId)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                    onError()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraScreen", "Photo saved: ${photoFile.absolutePath}")
                    onImageCaptured()
                }
            }
        )
    } ?: run {
        Log.e("CameraScreen", "ImageCapture is null")
        onError()
    }
}
