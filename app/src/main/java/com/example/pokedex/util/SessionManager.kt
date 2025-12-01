package com.example.pokedex.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_AUTH_TOKEN = "auth_token" // Nova constante para o token
    }

    // Salvar login e token
    fun saveUserSession(login: String, token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_LOGIN, login)
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun getUserLogin(): String? {
        return prefs.getString(KEY_USER_LOGIN, null)
    }

    // Novo método para recuperar o token
    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}