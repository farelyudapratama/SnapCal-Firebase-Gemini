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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.messaging.FirebaseMessaging
import com.yuch.snapcalfirebasegemini.viewmodel.AnnouncementViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.AuthState
import com.yuch.snapcalfirebasegemini.viewmodel.AuthViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.CameraViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.FoodListViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.OnboardingViewModel
import com.yuch.snapcalfirebasegemini.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

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
    private val viewModelFactory by lazy {
        (application as SnapcalApplication).container.viewModelFactory
    }

    private val authViewModel: AuthViewModel by viewModels { viewModelFactory }
    private val cameraViewModel: CameraViewModel by viewModels()
    
    // Gunakan factory yang sama
    private val getFoodViewModel: FoodListViewModel by viewModels { viewModelFactory }
    private val announcementViewModel: AnnouncementViewModel by viewModels { viewModelFactory }
    
    private val profileViewModel: ProfileViewModel by viewModels { viewModelFactory }
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private var lastAuthState: AuthState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        createNotificationChannel()
        askNotificationPermission()
        subscribeToTopic()
        observeSessionState()
        
        announcementViewModel.fetchAnnouncements()

        setContent {
            MaterialTheme {
                SnapCalApp(
                    authViewModel = authViewModel, 
                    cameraViewModel = cameraViewModel, 
                    getFoodViewModel = getFoodViewModel,
                    profileViewModel = profileViewModel, 
                    onboardingViewModel = onboardingViewModel, 
                    announcementViewModel = announcementViewModel,
                    viewModelFactory = viewModelFactory // Passing factory ke bawah
                )
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

    private fun observeSessionState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { authState ->
                    if (lastAuthState is AuthState.Authenticated && authState is AuthState.Unauthenticated) {
                        clearUserData()
                    }
                    lastAuthState = authState
                }
            }
        }
    }

    private fun clearUserData() {
        cameraViewModel.clearData()
        getFoodViewModel.clearData()
        profileViewModel.clearData()
        onboardingViewModel.clearData()
    }
}
