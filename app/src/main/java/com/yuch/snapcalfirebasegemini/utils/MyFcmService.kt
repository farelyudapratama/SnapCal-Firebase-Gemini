package com.yuch.snapcalfirebasegemini.utils

import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yuch.snapcalfirebasegemini.R

class MyFcmService : FirebaseMessagingService() {

    // Fungsi ini jalan kalau ada pesan masuk saat aplikasi di foreground
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            showNotification(it.title ?: "SnapCal", it.body ?: "")
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "default_channel" // SESUAIKAN dengan ID di MainActivity
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Pakai icon yang ada dulu buates
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    // Fungsi ini buat dapetin token unik HP kamu
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_LOG", "Token baru: $token")
        // Token ini yang dipakai buat nembak notif ke HP spesifik
    }
}