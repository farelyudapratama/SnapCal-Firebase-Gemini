package com.yuch.snapcalfirebasegemini

import android.app.Application
import com.yuch.snapcalfirebasegemini.di.AppContainer

class SnapcalApplication : Application() {

    // Instance ini akan tersedia di seluruh siklus hidup aplikasi
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi container saat aplikasi pertama kali dibuka
        container = AppContainer(this)
    }
}