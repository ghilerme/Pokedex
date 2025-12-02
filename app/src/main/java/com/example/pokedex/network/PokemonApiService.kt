package com.example.pokedex.network

import com.example.pokedex.model.DashboardResponse
import com.example.pokedex.model.LoginResponse
import com.example.pokedex.model.Pokemon
import com.example.pokedex.model.PokemonListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PokemonApiService {

    // --- AUTENTICAÇÃO ---

    @POST("login")
    suspend fun login(@Body loginData: Map<String, String>): Response<LoginResponse>

    // --- DASHBOARD ---
    @GET("pokemon/dashboard")
    suspend fun getDashboardStats(
        @Header("Authorization") token: String
    ): Response<DashboardResponse>

    // --- LISTAGEM ---
    @GET("pokemon")
    suspend fun getPokemons(
        @Header("Authorization") token: String
    ): Response<PokemonListResponse>

    // --- PESQUISA ---
    @GET("pokemon")
    suspend fun searchPokemons(
        @Header("Authorization") token: String,
        @Query("type") type: String?,
        @Query("ability") ability: String?
    ): Response<PokemonListResponse>

    // --- CADASTRO/EDIÇÃO ---

    @POST("pokemon")
    suspend fun createPokemon(
        @Header("Authorization") token: String,
        @Body pokemon: Pokemon
    ): Response<Void>

    @PUT("pokemon/{id}")
    suspend fun updatePokemon(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body pokemon: Pokemon
    ): Response<Void>

    @DELETE("pokemon/{id}")
    suspend fun deletePokemon(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Void>
}