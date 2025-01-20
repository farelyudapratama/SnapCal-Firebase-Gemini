package com.yuch.snapcalfirebasegemini.view.camera

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel

@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: CameraViewModel,
    onBack: () -> Unit
) {
    val authState = authViewModel.authState.observeAsState()

    // State untuk menyimpan pesan toast
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Efek untuk menampilkan toast
    val context = LocalContext.current
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null // Reset setelah menampilkan toast
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
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    onImageCaptured = { imagePath ->
                        viewModel.onTakePhoto(imagePath)
                        toastMessage = "Gambar disimpan di $imagePath"
                    },
                    onError = { error ->
                        toastMessage = "Error: ${error.message}"
                    }
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                }

                val bitmaps by viewModel.bitmaps.collectAsState()
                bitmaps?.lastOrNull()?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Preview Gambar",
                        modifier = Modifier.fillMaxSize()
                    )
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
