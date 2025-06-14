package com.yuch.snapcalfirebasegemini.data.repository

import android.util.Log
import com.yuch.snapcalfirebasegemini.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository untuk menangani operasi Firebase seperti autentikasi dan penyimpanan data
 */
class FirebaseRepository {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    /**
     * Mendapatkan user yang saat ini terautentikasi
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Melakukan login dengan email dan password
     */
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    /**
     * Melakukan login dengan email dan password (menggunakan coroutines)
     */
    suspend fun loginAsync(email: String, password: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                Result.success(authResult.user!!)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Login failed", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Melakukan pendaftaran dengan email dan password
     */
    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    /**
     * Melakukan pendaftaran dengan email dan password (menggunakan coroutines)
     */
    suspend fun registerAsync(email: String, password: String): Result<FirebaseUser> {
        return withContext(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                Result.success(authResult.user!!)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Registration failed", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Mengambil data pengguna
     */
    fun getUserData(uid: String, onResult: (User?) -> Unit) {
        database.getReference("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                onResult(user)
            }
    }

    /**
     * Melakukan logout
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Mengirim email untuk reset password
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("FirebaseRepository", "Password reset failed", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Mendapatkan token Firebase
     */
    suspend fun getFirebaseToken(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val tokenResult = user.getIdToken(true).await()
                    Result.success(tokenResult.token!!)
                } else {
                    Result.failure(Exception("User not authenticated"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: FirebaseRepository? = null

        fun getInstance(): FirebaseRepository =
            instance ?: synchronized(this) {
                instance ?: FirebaseRepository().also { instance = it }
            }
    }
}
