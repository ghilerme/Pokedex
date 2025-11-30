package com.example.pokedex.network

import retrofit2.Response
import retrofit2.http.*
import com.example.pokedex.model.DashboardStats
import com.example.pokedex.model.Pokemon

interface PokemonApiService {

    @POST("auth/login")
    suspend fun login(@Body loginData: Map<String, String>): Response<Void>

    @GET("dashboard")
    suspend fun getDashboardStats(): Response<DashboardStats>

    @GET("pokemons")
    suspend fun getPokemons(): Response<List<Pokemon>>


    @GET("pokemons/search")
    suspend fun searchPokemons(
        @Query("term") term: String,   // O termo digitado
        @Query("type") type: String    // "tipo" ou "habilidade"
    ): Response<List<Pokemon>>

    @POST("pokemons")
    suspend fun createPokemon(@Body pokemon: Pokemon): Response<Void>

    @PUT("pokemons/{id}")
    suspend fun updatePokemon(@Path("id") id: Int, @Body pokemon: Pokemon): Response<Void>

    @DELETE("pokemons/{id}")
    suspend fun deletePokemon(@Path("id") id: Int): Response<Void>
}