package com.yuch.snapcalfirebasegemini.view.camera

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import kotlinx.coroutines.launch
import java.io.File

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
                            // Navigate to preview screen after capture
                            navController.navigate("preview/$imagePath")
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
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
                modifier = Modifier
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