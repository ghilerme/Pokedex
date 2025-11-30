package com.example.pokedex.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_LOGIN = "user_login"
    }

    fun saveUserSession(login: String) {
        prefs.edit().putString(KEY_USER_LOGIN, login).apply()
    }

    fun getUserLogin(): String? {
        return prefs.getString(KEY_USER_LOGIN, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}