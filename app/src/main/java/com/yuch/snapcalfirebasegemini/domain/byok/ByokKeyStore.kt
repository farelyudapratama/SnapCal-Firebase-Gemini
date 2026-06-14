package com.yuch.snapcalfirebasegemini.domain.byok

import android.content.Context
import android.content.SharedPreferences

object ByokKeyStore {
    private const val PREFS_NAME = "byok_keys"
    private const val KEY_GEMINI = "gemini_key"
    private const val KEY_GROQ = "groq_key"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            synchronized(this) {
                if (prefs == null) {
                    prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                }
            }
        }
    }

    fun getGeminiKey(): String = prefs?.getString(KEY_GEMINI, null).orEmpty()

    fun getGroqKey(): String = prefs?.getString(KEY_GROQ, null).orEmpty()

    fun saveKeys(geminiKey: String, groqKey: String) {
        prefs?.edit()
            ?.putString(KEY_GEMINI, geminiKey.trim())
            ?.putString(KEY_GROQ, groqKey.trim())
            ?.apply()
    }

    fun clearKeys() {
        prefs?.edit()
            ?.remove(KEY_GEMINI)
            ?.remove(KEY_GROQ)
            ?.apply()
    }

    fun hasAnyKey(): Boolean = getGeminiKey().isNotBlank() || getGroqKey().isNotBlank()
}
