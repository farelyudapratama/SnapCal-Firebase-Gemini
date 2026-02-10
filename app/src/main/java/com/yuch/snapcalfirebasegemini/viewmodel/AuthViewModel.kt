package com.yuch.snapcalfirebasegemini.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    // Callback untuk menghapus data di ViewModels lain
    private var clearDataCallback: (() -> Unit)? = null

    init {
        checkAuthStatus()
    }

    fun setClearDataCallback(callback: () -> Unit) {
        clearDataCallback = callback
    }

    private fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
            _userEmail.value = auth.currentUser?.email
        }
    }

    fun login(email : String,password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    _userEmail.value = auth.currentUser?.email
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signup(email : String,password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    _userEmail.value = auth.currentUser?.email
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
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


    fun signout(){
        // Hapus semua data sebelum logout
        clearAllUserData()
        
        // Clear cached token
        ApiConfig.clearTokenCache()

        auth.signOut()
        _authState.value = AuthState.Unauthenticated

        // Reset data di AuthViewModel
        _userEmail.value = null
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
    data object PasswordResetSent : AuthState()
    data object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}