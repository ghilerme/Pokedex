package com.example.pokedex.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Pokemon(
    val id: String? = null,           // Mude de Int para String
    @SerializedName("name")           // Mapeie "name" (API) para "nome" (App)
    val nome: String,
    @SerializedName("type")
    val tipo: String,
    @SerializedName("abilities")      // Mapeie "abilities" para List<String>
    val habilidades: List<String>,    // Mude de String para List<String>
    @SerializedName("createdBy")
    val usuario_cadastro: String? = null
) : Serializable
