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

    // StateFlow untuk menyimpan status kamera depan atau belakang
    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera = _isFrontCamera.asStateFlow()

    // Fungsi untuk menangani pengambilan foto
//    fun onTakePhoto(bitmap: String, imagePath: String) {
//        _bitmaps.value += bitmap
//        _capturedImagePaths.value += imagePath
//    }

    fun onTakePhoto(imagePath: String) {
        // Menambahkan path gambar ke list
        _capturedImagePaths.value += imagePath
    }

    fun saveBitmapToFile(bitmap: Bitmap): String {
        // Simpan bitmap ke file
        return ""
    }

    fun toggleCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun clearData() {
        _bitmaps.value = emptyList()
        _capturedImagePaths.value = emptyList()
        _isFrontCamera.value = false
    }
}