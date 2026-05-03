package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// State untuk UI Action
sealed class AuthActionState {
    object Idle : AuthActionState()
    object Loading : AuthActionState()
    object SuccessVerification : AuthActionState() // Email Verified
    object SuccessReset : AuthActionState()        // Password Changed
    class ResetPasswordMode(val code: String) : AuthActionState() // Mode Input Password Baru
    class Error(val message: String) : AuthActionState()
}

class AuthActionViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthActionState>(AuthActionState.Idle)
    val uiState: StateFlow<AuthActionState> = _uiState.asStateFlow()

    // Fungsi Utama: Dipanggil saat layar dibuka dari link
    fun handleDeepLink(mode: String?, oobCode: String?) {
        if (mode == null || oobCode == null) {
            _uiState.value = AuthActionState.Error("Link tidak valid atau rusak.")
            return
        }

        when (mode) {
            "verifyEmail" -> verifyEmail(oobCode)
            "resetPassword" -> {
                // Jangan langsung panggil Firebase, tampilkan Form dulu ke user
                _uiState.value = AuthActionState.ResetPasswordMode(oobCode)
            }
            else -> _uiState.value = AuthActionState.Error("Mode aksi tidak dikenali: $mode")
        }
    }

    private fun verifyEmail(code: String) {
        _uiState.value = AuthActionState.Loading
        auth.applyActionCode(code)
            .addOnSuccessListener {
                // Reload user jika sedang login agar status verified terupdate
                auth.currentUser?.reload()
                _uiState.value = AuthActionState.SuccessVerification
            }
            .addOnFailureListener {
                _uiState.value = AuthActionState.Error(it.message ?: "Gagal memverifikasi email.")
            }
    }

    // Dipanggil saat user klik tombol "Simpan Password Baru"
    fun confirmPasswordReset(code: String, newPass: String) {
        if (newPass.length < 6) {
            _uiState.value = AuthActionState.Error("Password minimal 6 karakter")
            return
        }

        _uiState.value = AuthActionState.Loading
        auth.confirmPasswordReset(code, newPass)
            .addOnSuccessListener {
                _uiState.value = AuthActionState.SuccessReset
            }
            .addOnFailureListener {
                _uiState.value = AuthActionState.Error(it.message ?: "Gagal mereset password.")
            }
    }
}
