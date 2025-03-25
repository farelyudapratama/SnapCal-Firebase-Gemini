package com.yuch.snapcalfirebasegemini.view

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val email by authViewModel.userEmail.observeAsState("")
    val context  = LocalContext.current
    val queue = remember { Volley.newRequestQueue(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo(0)
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Page", fontSize = 32.sp)

        // Menampilkan email pengguna
        if (email.isNotBlank()) {
            Text(text = "Login sebagai: $email", fontSize = 18.sp)
        } else {
            Text(text = "Memuat data pengguna...", fontSize = 18.sp)
        }

        TextButton(onClick = { authViewModel.signout() }) {
            Text(text = "Sign out")
        }

        Button(onClick = { authViewModel.getFirebaseToken() }) {
            Text("Test Firebase Token")
        }

        // Tombol untuk mendownload model & label
        Button(onClick = {
            scope.launch {
                fetchAndDownloadModel(context, queue)
            }
        }) {
            Text("Download Model & Label")
        }
    }
}

// Fungsi untuk mengambil Signed URL & mendownload file
fun fetchAndDownloadModel(context: Context, queue: RequestQueue) {
    val apiUrl = "https://backendsnapcalmd.vercel.app/v1/download/model"

    val request = JsonObjectRequest(
        Request.Method.GET, apiUrl, null,
        { response: JSONObject ->
            try {
                val modelUrl = response.getString("model_url")
                val labelsUrl = response.getString("labels_url")

                // Download Model & Label
                downloadFile(context, modelUrl, "model_unquant.tflite")
                downloadFile(context, labelsUrl, "labels.txt")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        { error -> error.printStackTrace() }
    )

    queue.add(request)
}

// Fungsi untuk mendownload file dari URL menggunakan DownloadManager
fun downloadFile(context: Context, fileUrl: String, fileName: String) {
    val request = DownloadManager.Request(Uri.parse(fileUrl))
        .setTitle(fileName)
        .setDescription("Downloading $fileName...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

    Log.d("Download", "Downloading: $fileName")
}
