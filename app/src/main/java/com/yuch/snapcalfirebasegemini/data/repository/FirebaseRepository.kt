package com.yuch.snapcalfirebasegemini.data.repository

import com.yuch.snapcalfirebasegemini.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    internal val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Fungsi untuk login
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Fungsi untuk register
    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Fungsi untuk mengambil data pengguna
    fun getUserData(uid: String, onResult: (User?) -> Unit) {
        database.getReference("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(
                    User::class.java)
                onResult(user)
            }
    }
}
