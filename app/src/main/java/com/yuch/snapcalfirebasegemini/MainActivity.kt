package com.yuch.snapcalfirebasegemini

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.room.Room
import com.google.firebase.messaging.FirebaseMessaging
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.yuch.snapcalfirebasegemini.data.local.AppDatabase
import com.yuch.snapcalfirebasegemini.data.repository.ApiRepository
import com.yuch.snapcalfirebasegemini.data.repository.ProfileViewModelFactory
import com.yuch.snapcalfirebasegemini.data.repository.ViewModelFactory
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.GetFoodViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Izin diberikan!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Izin ditolak. Kamu tidak akan menerima notifikasi.", Toast.LENGTH_SHORT).show()
        }
    }


    private val authViewModel: AuthViewModel by viewModels()
    private val cameraViewModel: CameraViewModel by viewModels()
    private val getFoodViewModel: GetFoodViewModel by viewModels {
        ViewModelFactory(
            ApiRepository(
                apiService = ApiConfig.getApiService(),
                foodDao = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "snapcal_database"
                ).build().foodDao()
            )
        )
    }
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ApiConfig.getApiService())
    }
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        createNotificationChannel()
        askNotificationPermission()
        subscribeToTopic()
        // Setup callback untuk menghapus semua data saat logout
        setupClearDataCallback()

        setContent {
            MaterialTheme {
                SnapCalApp(authViewModel = authViewModel, cameraViewModel = cameraViewModel, getFoodViewModel = getFoodViewModel,
                    profileViewModel = profileViewModel, onboardingViewModel = onboardingViewModel)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel"
            val name = "Notifikasi SnapCal"
            val descriptionText = "Channel untuk notifikasi info kalori"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("announcements_all")
    }

    private fun setupClearDataCallback() {
        authViewModel.setClearDataCallback {
            // Hapus data dari semua ViewModels
            cameraViewModel.clearData()
            getFoodViewModel.clearData()
            profileViewModel.clearData()
            onboardingViewModel.clearData()
        }
    }
}
