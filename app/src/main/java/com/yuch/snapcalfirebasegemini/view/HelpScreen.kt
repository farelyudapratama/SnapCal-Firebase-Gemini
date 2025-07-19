package com.yuch.snapcalfirebasegemini.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bantuan & FAQ") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                FAQItem(
                    question = "Bagaimana cara menambahkan makanan?",
                    answer = "Anda dapat menambahkan makanan dengan dua cara:\n" +
                            "1. Tekan tombol '+' di layar utama, lalu pilih 'Pindai Makanan' untuk mengambil foto makanan Anda. AI kami akan menganalisisnya secara otomatis.\n" +
                            "2. Jika Anda tahu detail nutrisinya, pilih 'Entri Manual' dan isi informasinya sendiri."
                )
            }
            item {
                FAQItem(
                    question = "Apakah analisis makanan dari foto selalu akurat?",
                    answer = "Analisis AI kami cukup canggih, namun tingkat akurasi sangat dipengaruhi oleh kualitas foto dan jenis makanan. Karena model kami saat ini hanya mengklasifikasi 25 kelas gambar makanan, hasil analisis bisa saja kurang tepat. Anda selalu dapat meninjau dan mengedit hasil sebelum menyimpannya untuk memastikan data yang masuk akurat."
                )
            }
            item {
                FAQItem(
                    question = "Bagaimana cara melihat ringkasan nutrisi mingguan saya?",
                    answer = "Buka tab 'Riwayat' dari bilah navigasi bawah. Di sana Anda akan menemukan ringkasan asupan nutrisi Anda selama seminggu terakhir."
                )
            }
            item {
                FAQItem(
                    question = "Bagaimana cara mengubah target kalori harian saya?",
                    answer = "Buka tab 'Profil', lalu di bagian 'Informasi Pengguna', Anda akan menemukan target kalori harian Anda. Tekan tombol edit di sampingnya untuk menyesuaikan target sesuai kebutuhan Anda."
                )
            }
            item {
                FAQItem(
                    question = "Apa gunanya mengatur preferensi makanan di profil?",
                    answer = "Dengan mengatur preferensi seperti alergi, kondisi kesehatan, atau makanan yang tidak disukai, aplikasi dapat memberikan rekomendasi yang lebih personal dan akurat untuk Anda di masa mendatang."
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

