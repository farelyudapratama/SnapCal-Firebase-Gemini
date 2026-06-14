package com.yuch.snapcalfirebasegemini.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun uriToFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = kotlin.io.path.createTempFile(suffix = ".jpg").toFile()

    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }

    return file
}
