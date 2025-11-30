package com.example.pokedex.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PokedexPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_LOGIN = "user_login"
    }

    // Salva o login para usar nas telas de cadastro
    fun saveUserSession(login: String) {
        prefs.edit().putString(KEY_USER_LOGIN, login).apply()
    }

    // Recupera o login
    fun getUserLogin(): String? {
        return prefs.getString(KEY_USER_LOGIN, null)
    }

    // Limpa a sessão (para o botão Sair)
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}