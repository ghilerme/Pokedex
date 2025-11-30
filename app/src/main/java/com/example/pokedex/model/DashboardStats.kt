package com.example.pokedex.model

data class DashboardStats(
    val totalPokemons: Int,
    val topTipos: List<String>,
    val topHabilidades: List<String>
)