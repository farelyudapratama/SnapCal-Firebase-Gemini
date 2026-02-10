# 🍽️ SnapCal — Pelacak Nutrisi Berbasis AI

> **Platform:** Android (Mobile) & Web  
> **Min SDK:** 26 (Android 8.0+)

SnapCal adalah aplikasi pelacakan nutrisi yang menggunakan AI dan computer vision untuk menganalisis makanan dari foto. Cukup foto makananmu, dapatkan informasi nutrisi lengkap, lacak asupan harian, dan terima rekomendasi makanan yang dipersonalisasi — semua didukung oleh YOLO object detection, Google Gemini, dan Groq.

📖 **[Read in English](README.md)**

---

## ✨ Fitur

- **📸 Analisis Makanan AI** — Ambil foto atau pilih dari galeri, AI akan mengenali makanan dan memperkirakan kandungan nutrisinya.
- **🤖 Dual AI Pipeline** — Model YOLO untuk deteksi cepat, dengan Google Gemini / Groq sebagai fallback untuk analisis yang lebih kompleks.
- **📊 Pelacakan Nutrisi** — Lacak kalori, karbohidrat, protein, lemak, serat, gula, dan lainnya secara harian/mingguan.
- **🍳 Rekomendasi Makanan** — Dapatkan saran sarapan, makan siang, dan makan malam yang dipersonalisasi berdasarkan profil dan tujuan kesehatanmu.
- **💬 AI Chat** — Ngobrol dengan AI nutritionist untuk saran makanan dan diet.
- **👤 Profil Pengguna** — Atur tujuan kesehatan, preferensi diet, alergi, dan tingkat aktivitas.
- **✏️ Input Manual** — Tambahkan data makanan secara manual kalau mau.
- **📅 Tampilan Kalender** — Lihat riwayat makanan berdasarkan tanggal.
- **📢 Pengumuman** — Tetap update dengan pengumuman dan tips dari aplikasi.
- **🔔 Push Notification** — Dukungan Firebase Cloud Messaging.

---

## 🏗️ Tech Stack

| Layer | Teknologi |
|-------|----------|
| **Bahasa** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Arsitektur** | MVVM (Model-View-ViewModel) |
| **Navigasi** | Jetpack Navigation Compose |
| **Networking** | Retrofit 2 + OkHttp 4 |
| **Database Lokal** | Room |
| **Image Loading** | Coil |
| **Kamera** | CameraX |
| **Autentikasi** | Firebase Authentication |
| **Push Notification** | Firebase Cloud Messaging |
| **Remote Config** | Firebase Remote Config |
| **Chart** | MPAndroidChart |
| **AI / ML** | YOLO (custom model), Google Gemini, Groq |
| **Build System** | Gradle (Kotlin DSL) + Version Catalog |

---

## 🚀 Cara Memulai

### Prasyarat

- **Android Studio** Ladybug (2024.2.1) atau lebih baru
- **JDK 11+**
- **Android SDK 35**
- Project Firebase dengan Authentication diaktifkan
- API key untuk backend service

### Setup

1. **Clone repo**
   ```bash
   git clone https://github.com/farelyudapratama/SnapCal-Firebase-Gemini.git
   cd SnapCal-Firebase-Gemini
   ```

2. **Setup Firebase**
   - Buat project Firebase di [Firebase Console](https://console.firebase.google.com/).
   - Aktifkan autentikasi **Email/Password**.
   - Download `google-services.json` dan taruh di folder `app/`.

3. **Backend API**
   - Aplikasi terhubung ke backend API untuk analisis makanan, rekomendasi, dan chat.
   - Update base URL di `ApiConfig.kt` atau via Firebase Remote Config.

4. **Build & Jalankan**
   ```bash
   ./gradlew assembleDebug
   ```
   Atau langsung tekan ▶️ di Android Studio.

---

## 🔧 Gambaran Arsitektur

Aplikasi mengikuti pattern **MVVM** dengan pemisahan concern yang jelas:

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   View       │===> │  ViewModel   │====>│  Repository  │
│ (Composable) │<====│  (StateFlow) │<====│  (API/Room)  │
└──────────────┘     └──────────────┘     └──────────────┘
```

**Pattern yang digunakan:**
- **StateFlow** untuk reactive state management (sudah migrasi dari LiveData).
- **Sealed class** (`AuthState`, `ProfileState`, `RecommendationState`) untuk UI state yang type-safe.
- **Unified ViewModelFactory** sebagai dependency injection terpusat untuk semua ViewModel.
- **Repository pattern** — ViewModel tidak pernah memanggil `ApiService` secara langsung.
- **Token caching** dengan auto-refresh via OkHttp `Authenticator`.

---

## 🔑 Alur API & Autentikasi

1. User login via **Firebase Authentication** (email/password).
2. Firebase mengembalikan JWT token.
3. Token di-cache dan dilampirkan ke setiap API request via OkHttp interceptor.
4. Saat dapat respons 401, `Authenticator` otomatis me-refresh token.
5. Semua API call melewati `ApiRepository` → `ApiService` → Backend.

---

## 🤖 Pipeline Analisis AI

Saat user mengambil foto makanan:

1. **Deteksi YOLO** — Gambar dikirim ke backend, yang menjalankan model YOLO custom untuk deteksi objek makanan.
2. **Fallback Gemini / Groq** — Kalau YOLO tidak mendeteksi apapun, backend fallback ke Google Gemini atau Groq untuk analisis berbasis AI.
3. **Estimasi Nutrisi** — AI mengembalikan estimasi nilai nutrisi (kalori, makro, dll).
4. **Review User** — User bisa melihat, mengedit, dan menyimpan hasil analisis.

---

## 📝 Kontribusi

1. Fork repo ini.
2. Buat feature branch: `git checkout -b feature/fitur-keren`
3. Commit perubahan: `git commit -m 'Tambah fitur keren'`
4. Push: `git push origin feature/fitur-keren`
5. Buka Pull Request.

### Coding Style

- Taruh composable di `ui/components/` kalau reusable, atau di `view/` kalau spesifik untuk satu screen.
- Semua ViewModel baru harus pakai `ApiRepository` (bukan `ApiService` langsung) dan didaftarkan di `ViewModelFactory`.
- Gunakan `StateFlow` untuk state management, bukan `LiveData`.
- Error message di ViewModel harus dalam bahasa Inggris (lokalisasi dilakukan di UI layer via string resources).

---

**Terimakasih Sudah mampir disini**
