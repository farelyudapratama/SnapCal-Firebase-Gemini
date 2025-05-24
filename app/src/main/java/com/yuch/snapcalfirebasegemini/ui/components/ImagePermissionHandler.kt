package com.yuch.snapcalfirebasegemini.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

object PermissionUtils {
    fun getRequiredPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}

@Composable
fun rememberPermissionState(
    onPermissionResult: (Boolean) -> Unit
): PermissionState {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    return remember(context, permissionLauncher) {
        PermissionState(
            context = context,
            permission = PermissionUtils.getRequiredPermission(),
            permissionLauncher = permissionLauncher,
            onPermissionResult = onPermissionResult
        )
    }
}

class PermissionState(
    private val context: Context,
    private val permission: String,
    private val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    private val onPermissionResult: (Boolean) -> Unit
) {
    fun requestPermission() {
        when {
            // Permission is already granted
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> {
                onPermissionResult(true)
            }
            // Should show rationale
            (context as? Activity)?.shouldShowRequestPermissionRationale(permission) == true -> {
                permissionLauncher.launch(permission)
            }
            // First time asking for permission or permanently denied
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    fun checkPermission(): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}

@Composable
fun ImagePermissionHandler(
    onPermissionGranted: () -> Unit,
    content: @Composable (showPermissionDialog: () -> Unit) -> Unit
) {
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionState = rememberPermissionState { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            showSettingsDialog = true
        }
    }

    // Check initial permission state
    LaunchedEffect(Unit) {
        if (permissionState.checkPermission()) {
            onPermissionGranted()
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Permission Required") },
            text = {
                Text("This app needs access to your gallery to select images. " +
                        "This permission is used only for selecting images you choose to upload.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        permissionState.requestPermission()
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permission Required") },
            text = {
                Text("To select images, you need to enable gallery access in Settings. " +
                        "Would you like to open Settings now?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                        permissionState.openAppSettings()
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    content { showRationaleDialog = true }
}