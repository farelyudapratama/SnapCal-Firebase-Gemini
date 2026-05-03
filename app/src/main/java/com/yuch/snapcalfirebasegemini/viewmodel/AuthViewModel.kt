package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    // Form State
    private val _emailInput = MutableStateFlow("")
    val emailInput = _emailInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("")
    val passwordInput = _passwordInput.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible = _passwordVisible.asStateFlow()

    // Validation Logic
    val isEmailValid: StateFlow<Boolean> = _emailInput.map { email ->
        email.trim().matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) || email.isEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isPasswordValid: StateFlow<Boolean> = _passwordInput.map { password ->
        password.length >= 6 || password.isEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isFormValid: StateFlow<Boolean> = combine(isEmailValid, isPasswordValid, _emailInput, _passwordInput) { emailValid, passValid, email, pass ->
        emailValid && passValid && email.isNotEmpty() && pass.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Callback untuk menghapus data di ViewModels lain
    private var clearDataCallback: (() -> Unit)? = null

    init {
        checkAuthStatus()
    }

    fun onEmailChange(newEmail: String) {
        _emailInput.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _passwordInput.value = newPassword
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun setClearDataCallback(callback: () -> Unit) {
        clearDataCallback = callback
    }

    private fun checkAuthStatus(){
        val currentUser = auth.currentUser
        if(currentUser == null){
            _authState.value = AuthState.Unauthenticated
        } else {
            // Cek verifikasi email saat aplikasi dibuka (jika user masih login)
            if (currentUser.isEmailVerified) {
                _authState.value = AuthState.Authenticated
                _userEmail.value = currentUser.email
            } else {
                // Jika belum diverifikasi, paksa logout
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login() {
        val email = _emailInput.value
        val password = _passwordInput.value

        if (!isFormValid.value) return

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Login Sukses & Email Terverifikasi
                        _authState.value = AuthState.Authenticated
                        _userEmail.value = user.email
                    } else {
                        // Login Sukses tapi Email BELUM Terverifikasi
                        auth.signOut() // Tendang user keluar
                        _authState.value = AuthState.EmailNotVerified
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup() {
        val email = _emailInput.value
        val password = _passwordInput.value

        if (!isFormValid.value) return

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    // Kirim email verifikasi
                    sendEmailVerification()
                    
                    // Jangan login otomatis, minta user cek email dulu
                    auth.signOut()
                    _authState.value = AuthState.EmailNotVerified
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    // Reset Password
    fun sendPasswordReset(email: String) {
        _authState.value = AuthState.Loading

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { resetTask ->
                if (resetTask.isSuccessful) {
                    _authState.value = AuthState.PasswordResetSent
                } else {
                    val exception = resetTask.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                        else -> exception?.message ?: "Password reset failed"
                    }

                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }
    
    // Kirim Email Verifikasi (Perlu login sementara untuk kirim, lalu logout jika di flow manual)
    // Catatan: Fungsi ini biasanya dipanggil saat user baru saja dibuat (masih ada session aktif sebentar)
    // Atau jika kita mengizinkan login sementara untuk kirim ulang.
    // Untuk kasus "Dilarang Masuk", kita asumsikan ini dipanggil otomatis saat register.
    // Jika butuh "Resend", user harus login dulu (session tercipta), kirim, lalu logout.
    fun resendVerificationEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { 
                it.user?.sendEmailVerification()?.addOnCompleteListener { task ->
                    auth.signOut() // Logout lagi setelah kirim
                    if (task.isSuccessful) {
                         _authState.value = AuthState.Error("Email verifikasi telah dikirim ulang. Silakan cek inbox Anda.")
                    } else {
                        _authState.value = AuthState.Error("Gagal mengirim email verifikasi.")
                    }
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Gagal login untuk mengirim ulang email. Cek password Anda.")
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
    }


    fun signout(){
        // Hapus semua data sebelum logout
        clearAllUserData()
        
        // Clear cached token
        ApiConfig.clearTokenCache()

        auth.signOut()
        _authState.value = AuthState.Unauthenticated

        // Reset data di AuthViewModel
        _userEmail.value = null
        _emailInput.value = ""
        _passwordInput.value = ""
    }

    private fun clearAllUserData() {
        // Panggil callback untuk menghapus data di ViewModels lain
        clearDataCallback?.invoke()
    }

    fun resetState() {
        _authState.value = AuthState.Unauthenticated
    }
}


sealed class AuthState{
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
    data object EmailNotVerified : AuthState() // State Baru
    data object PasswordResetSent : AuthState()
    data object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}
