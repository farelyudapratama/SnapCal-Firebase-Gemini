# Penghapusan dan Penyesuaian Kode

Berikut adalah langkah-langkah yang telah dilakukan untuk menyelesaikan proses refactoring dan pengorganisasian kembali kode:

## File yang Dihapus
1. `GetFoodViewModel.kt` - telah dihapus karena fungsionalitasnya sudah digabungkan dengan `FoodViewModel`
2. `ApiRepository.kt` - telah dihapus karena fungsionalitasnya sudah digantikan oleh `FoodRepository`
3. `Injection.kt` - telah dihapus karena seluruhnya dikomentari dan tidak digunakan
4. Folder `di` - telah dihapus karena kosong setelah memindahkan semua factory ke `data/di`

## File yang Diperbarui
1. `ProfileScreen.kt` - pembaruan import dan parameter untuk menggunakan `FoodViewModel` alih-alih `GetFoodViewModel`
2. `MainScreen.kt` - pembaruan import dan parameter untuk menggunakan `FoodViewModel` alih-alih `GetFoodViewModel`
3. `NutriTrackScreen.kt` - pembaruan import dan parameter untuk menggunakan `FoodViewModel` alih-alih `GetFoodViewModel`
4. `DetailFoodScreen.kt` - pembaruan import dan parameter untuk menggunakan `FoodViewModel` alih-alih `GetFoodViewModel`
5. `EditFoodScreen.kt` - pembaruan import dan menghapus parameter `getFoodViewModel`
6. `AppNavHost.kt` - menghapus parameter `getFoodViewModel` dari pemanggilan EditFoodScreen

## Perubahan pada Struktur Kode
1. `ViewModel` - mengorganisasi kembali ViewModel dengan menerapkan pola repository yang konsisten
   - `FoodViewModel` - menggabungkan fungsionalitas dari FoodViewModel dan GetFoodViewModel
   - `OnboardingViewModel` - ditingkatkan dengan logika kalkulasi dan integrasi yang lebih baik dengan ProfileViewModel

2. `Model` - membersihkan duplikasi kelas model
   - Menghapus definisi duplikat `UpdateFoodData` dari NutritionData.kt

2. `Factory Classes` - mengorganisasi kelas ViewModelFactory ke dalam package `data/di`
   - Menggunakan pola singleton yang konsisten dengan metode `getInstance()`
   - Memastikan semua factory mengikuti pola yang sama

3. `Repository Pattern` - memastikan semua repository mengikuti pola singleton yang konsisten dengan `getInstance()`

4. `Integrasi` - membuat utilitas `ProfileOnboardingConnector` untuk memfasilitasi transfer data antara ViewModels terkait

## Manfaat Refaktoring
1. Mengurangi duplikasi kode
2. Meningkatkan keterbacaan dan pemeliharaan
3. Konsistensi pola desain di seluruh aplikasi
4. Penanganan kesalahan yang lebih baik dan terstandarisasi
5. API yang lebih jelas dan intuitif untuk komponen konsumsi
