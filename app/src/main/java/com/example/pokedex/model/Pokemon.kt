package com.example.pokedex.model

data class Pokemon(
    val id: Int? = null,
    val nome: String,
    val tipo: String,
    val habilidades: String,
    val usuario_cadastro: String? = null
)