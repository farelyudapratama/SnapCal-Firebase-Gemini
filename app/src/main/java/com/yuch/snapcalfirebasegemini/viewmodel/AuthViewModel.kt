package com.yuch.snapcalfirebasegemini.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.yuch.snapcalfirebasegemini.data.api.ApiConfig
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.yuch.snapcalfirebasegemini.R

class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _firebaseToken = MutableLiveData<String?>()
    val firebaseToken: MutableLiveData<String?> = _firebaseToken

    private val _userUid = MutableLiveData<String?>()
    val userUid: LiveData<String?> = _userUid

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

    fun getFirebaseToken() {
        val user = auth.currentUser
        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                // Tampilkan di Logcat
                Log.d("FIREBASE_DEBUG", "Token berhasil didapatkan : Bearer $token")
//                Log.d("FIREBASE_DEBUG", "Token length: ${token?.length} characters")
//                Log.d("FIREBASE_DEBUG", "First 10 chars: ${token?.take(10)}...")

                // Untuk debugging - HAPUS SEBELUM PRODUCTION
                // _firebaseToken.postValue(token) // Uncomment jika mau tampilkan di UI

                return@addOnCompleteListener
            } else {
                Log.e("FIREBASE_DEBUG", "Error getting token:", task.exception)
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
        _firebaseToken.value = null
        _userUid.value = null
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