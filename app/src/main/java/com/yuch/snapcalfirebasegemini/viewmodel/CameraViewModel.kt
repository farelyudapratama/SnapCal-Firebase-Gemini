package com.yuch.snapcalfirebasegemini.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {
    // StateFlow untuk menyimpan daftar Bitmap (preview)
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    // StateFlow untuk menyimpan daftar path gambar (lokasi penyimpanan)
    private val _capturedImagePaths = MutableStateFlow<List<String>>(emptyList())
    val capturedImagePaths = _capturedImagePaths.asStateFlow()

    // Fungsi untuk menangani pengambilan foto
//    fun onTakePhoto(bitmap: String, imagePath: String) {
//        _bitmaps.value += bitmap
//        _capturedImagePaths.value += imagePath
//    }

    fun onTakePhoto(imagePath: String) {
        // Menambahkan path gambar ke list
        _capturedImagePaths.value += imagePath
    }
}