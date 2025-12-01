package com.example.pokedex.model

import com.google.gson.annotations.SerializedName

// --- DASHBOARD (Usado na DashboardActivity) ---
// Retorno do endpoint /pokemon/dashboard [cite: 83]
data class DashboardResponse(
    val success: Boolean,
    val stats: DashboardStats
)

data class TopItem(
    val name: String,
    val count: Int
)

// --- LOGIN (Usado no Login) ---
// Retorno do endpoint /login [cite: 31]
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class User(
    val id: String,
    val login: String,
    val name: String
)

// --- LISTAGEM (Usado na Lista e Pesquisa) ---
// Retorno do endpoint /pokemon [cite: 118]
data class PokemonListResponse(
    val success: Boolean,
    // A API retorna a lista dentro de "pokemon" [cite: 121]
    val pokemon: List<Pokemon>
)