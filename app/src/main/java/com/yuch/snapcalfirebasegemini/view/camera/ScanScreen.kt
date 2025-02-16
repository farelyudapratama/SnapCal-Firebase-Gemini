package com.yuch.snapcalfirebasegemini.view.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.ui.components.openAppSettings
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: CameraViewModel,
    onBack: () -> Unit
) {
    val authState = authViewModel.authState.observeAsState()
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val isFrontCamera by viewModel.isFrontCamera.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // Handle selected image
            processSelectedImage(context, it, viewModel, navController)
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    CameraPermissionHandler(
        onPermissionGranted = {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 0.dp,
                sheetContent = {
                    PhotoBottomSheetContent(
                        bitmaps = viewModel.bitmaps.collectAsState().value
                    )
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    var cameraPreviewTakePhoto by remember { mutableStateOf<(() -> Unit)?>(null) }

                    CameraPreview(
                        onImageCaptured = { imagePath ->
                            viewModel.onTakePhoto(imagePath)
                            // Encode path untuk navigasi
                            val encodedPath = java.net.URLEncoder.encode(imagePath, "UTF-8")
                            navController.navigate("analyze/$encodedPath")
                        },
                        onError = { error ->
                            toastMessage = "Error: ${error.message}"
                        },
                        isFrontCamera = isFrontCamera,
                        onTakePhotoFunctionReady = { takePhotoFunction ->
                            cameraPreviewTakePhoto = takePhotoFunction
                        }
                    )

                    CameraSwitchButton(
                        onClick = viewModel::toggleCamera
                    )

                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            onClick = {
                                if (hasPermission) {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else {
                                    Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val activity = context as? Activity
                                        when {
                                            context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> {
                                                hasPermission = true
                                            }
                                            activity?.shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) == true -> {
                                                Toast.makeText(context, "Permission is required to access the gallery", Toast.LENGTH_LONG).show()
                                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                            }
                                            else -> {
                                                Toast.makeText(context, "Please enable permission in Settings", Toast.LENGTH_LONG).show()
                                                openAppSettings(context)
                                            }
                                        }
                                    } else {
                                        val activity = context as? Activity
                                        when {
                                            context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                                                hasPermission = true
                                            }
                                            activity?.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) == true -> {
                                                Toast.makeText(context, "Permission is required to access the gallery", Toast.LENGTH_LONG).show()
                                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                            }
                                            else -> {
                                                Toast.makeText(context, "Please enable permission in Settings", Toast.LENGTH_LONG).show()
                                                openAppSettings(context)
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Buka galeri",
                                tint = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                cameraPreviewTakePhoto?.invoke()
                            },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Ambil Foto",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        },
        onPermissionDenied = {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Izin kamera diperlukan untuk menggunakan fitur ini.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) {
                    Text("Kembali")
                }
            }
        }
    )
}

@Composable
fun CameraSwitchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    // Animasi untuk scaling saat ditekan
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Animasi untuk rotasi saat diklik
    var rotation by remember { mutableStateOf(0f) }
    val rotationAngle by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    IconButton(
        onClick = {
            onClick()
            rotation += 180f // Berputar 180 derajat setiap klik
        },
        modifier = modifier
            .scale(scale)
            .rotate(rotationAngle)
            .padding(16.dp)
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .size(48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Default.Cameraswitch,
            contentDescription = "Ganti Kamera",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .padding(4.dp)
        )
    }
}

private fun processSelectedImage(
    context: Context,
    uri: Uri,
    viewModel: CameraViewModel,
    navController: NavController
) {
    try {
        // Convert URI to File
        val file = uriToFile(context, uri)

        // Simpan ke ViewModel
        viewModel.onTakePhoto(file.absolutePath)

        // Encode path untuk navigasi
        val encodedPath = java.net.URLEncoder.encode(file.absolutePath, "UTF-8")

        // Navigasi ke preview screen
        navController.navigate("analyze/$encodedPath")
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = kotlin.io.path.createTempFile(suffix = ".jpg").toFile()

    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }

    return file
}
