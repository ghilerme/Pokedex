package com.example.pokedex.model

// Modelo unificado para GET (leitura) e POST (criação)
data class Pokemon(
    val id: String? = null, // Opcional, pois no cadastro não enviamos ID [cite: 147]
    val name: String,
    val type: String,
    // ATENÇÃO: A API exige que abilities seja uma Lista de Strings, não uma String única [cite: 126, 152]
    val abilities: List<String>,
    val createdBy: String? = null,
    val createdAt: String? = null
)