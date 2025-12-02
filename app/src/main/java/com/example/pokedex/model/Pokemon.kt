package com.example.pokedex.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Pokemon(
    val id: String? = null,
    @SerializedName("name")
    val nome: String,
    @SerializedName("type")
    val tipo: String,
    @SerializedName("description")
    val descricao: String? = null,
    @SerializedName("abilities")
    val habilidades: List<String>,
    @SerializedName("imageUrl")
    val imagemUrl: String? = null,
    @SerializedName("createdBy")
    val usuario_cadastro: String? = null
) : Serializable
