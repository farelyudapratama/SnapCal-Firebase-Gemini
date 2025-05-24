package com.yuch.snapcalfirebasegemini.view.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onImageCaptured: (String) -> Unit,
    onError: (Throwable) -> Unit,
    isFrontCamera: Boolean,
    onTakePhotoFunctionReady: ((()->Unit) -> Unit)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setJpegQuality(85) // Kompresi JPEG 85%
            .build()
    }

    // State untuk animasi fokus
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showFocusRing by remember { mutableStateOf(false) }

    // State untuk zoom level
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }
    var showZoomIndicator by remember { mutableStateOf(false) }

    val cameraSelector = remember(isFrontCamera) {
        if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
        else CameraSelector.DEFAULT_BACK_CAMERA
    }

    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<androidx.camera.core.CameraInfo?>(null) }

    // Reset focus animation setelah beberapa detik
    LaunchedEffect(showFocusRing) {
        if (showFocusRing) {
            kotlinx.coroutines.delay(1000)
            showFocusRing = false
        }
    }

    // Reset zoom indicator
    LaunchedEffect(showZoomIndicator) {
        if (showZoomIndicator) {
            kotlinx.coroutines.delay(1500)
            showZoomIndicator = false
        }
    }

    val takePhoto = {
        val photoFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            context.externalCacheDir ?: context.cacheDir
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val compressedFile = compressAndValidateImage(photoFile, isFrontCamera)
                        onImageCaptured(compressedFile.absolutePath)
                    } catch (e: Exception) {
                        onError(e)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    LaunchedEffect(takePhoto) {
        onTakePhotoFunctionReady(takePhoto)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        cameraControl?.let { control ->
                            val meteringPointFactory = previewView.meteringPointFactory
                            val meteringPoint = meteringPointFactory.createPoint(offset.x, offset.y)

                            val action = androidx.camera.core.FocusMeteringAction.Builder(meteringPoint)
                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                .build()

                            focusPoint = offset
                            showFocusRing = true

                            control.startFocusAndMetering(action)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        cameraControl?.let { control ->
                            val currentZoom = cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                            val newZoom = currentZoom * zoom
                            currentZoomRatio = newZoom.coerceIn(1f, 5f) // Batasi zoom antara 1x-5x
                            showZoomIndicator = true
                            control.setZoomRatio(currentZoomRatio)
                        }
                    }
                }
        ) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                try {
                    if (!cameraProvider.hasCamera(cameraSelector)) {
                        onError(Exception("Selected camera not available"))
                        return@addListener
                    }

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        cameraControl = camera.cameraControl
                        cameraInfo = camera.cameraInfo

                        // Aktifkan auto-focus untuk kamera depan
                        if (isFrontCamera) {
                            camera.cameraControl.enableTorch(false)
                            val factory = previewView.meteringPointFactory
                            val centerPoint = factory.createPoint(
                                previewView.width / 2f,
                                previewView.height / 2f
                            )
                            val action = androidx.camera.core.FocusMeteringAction.Builder(centerPoint)
                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                .build()
                            camera.cameraControl.startFocusAndMetering(action)
                        }
                    } catch (e: Exception) {
                        onError(e)
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Tampilkan indikator fokus
        if (showFocusRing && focusPoint != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            focusPoint!!.x.toInt() - 40,
                            focusPoint!!.y.toInt() - 40
                        )
                    }
                    .size(80.dp)
                    .border(
                        2.dp,
                        Color.Yellow,
                        CircleShape
                    )
            )
        }

        // Tampilkan indikator zoom
        if (showZoomIndicator) {
            Box(
                modifier = Modifier
                    .align(
                        Alignment.TopCenter)
                    .padding(top = 32.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = String.format("%.1fx", currentZoomRatio),
                    color = Color.White
                )
            }
        }
    }
}

// Fungsi kompresi dan validasi gambar
private fun compressAndValidateImage(file: File, isFrontCamera: Boolean): File {
    // Baca dan kompres gambar
    val options = BitmapFactory.Options().apply {
        inSampleSize = 2 // Downsample
    }

    var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

    // Flip horizontal untuk kamera depan
    if (isFrontCamera) {
        val matrix = Matrix().apply { postScale(-1f, 1f) }
        bitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    // Kompresi ulang
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.flush()
    }

    // Validasi ukuran file
    if (file.length() > 5 * 1024 * 1024) {
        throw Exception("Ukuran file melebihi 5MB setelah kompresi")
    }

    return file
}