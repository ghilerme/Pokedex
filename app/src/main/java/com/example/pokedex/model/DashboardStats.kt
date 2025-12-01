package com.example.pokedex.model

data class DashboardStats(
    val total: Int,
    val topTypes: List<TopItem>,
    val topAbilities: List<TopItem>
)