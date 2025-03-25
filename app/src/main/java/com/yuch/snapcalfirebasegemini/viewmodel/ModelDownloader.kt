package com.yuch.snapcalfirebasegemini.viewmodel

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ModelDownloader(private val context: Context) {

    private val apiUrl = "https://backendsnapcalmd.vercel.app/v1/download/model"

    fun fetchModelAndLabels() {
        val queue: RequestQueue = Volley.newRequestQueue(context)

        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response: JSONObject ->
                try {
                    val modelUrl = response.getString("model_url")
                    val labelsUrl = response.getString("labels_url")

                    // Download Model & Label
                    downloadFile(modelUrl, "model_unquant.tflite")
                    downloadFile(labelsUrl, "labels.txt")

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        )

        queue.add(request)
    }

    private fun downloadFile(fileUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Downloading $fileName...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Log.d("Download", "Downloading: $fileName")
    }
}
