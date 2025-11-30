package com.example.pokedex.network

import retrofit2.Response
import retrofit2.http.*
import com.example.pokedex.model.DashboardStats
import com.example.pokedex.model.Pokemon

interface PokemonApiService {

    // Autenticação [cite: 12]
    @POST("auth/login")
    suspend fun login(@Body loginData: Map<String, String>): Response<Void>

    // Dashboard
    @GET("dashboard")
    suspend fun getDashboardStats(): Response<DashboardStats>

    // Listar Todos [cite: 27]
    @GET("pokemons")
    suspend fun getPokemons(): Response<List<Pokemon>>

    // Pesquisar por Tipo ou Habilidade [cite: 33, 38]
    // Ex: /pokemons/search?tipo=fogo OU /pokemons/search?habilidade=voar
    @GET("pokemons/search")
    suspend fun searchPokemons(
        @Query("tipo") tipo: String?,
        @Query("habilidade") habilidade: String?
    ): Response<List<Pokemon>>

    // Cadastrar [cite: 20]
    @POST("pokemons")
    suspend fun createPokemon(@Body pokemon: Pokemon): Response<Void>

    // Detalhes, Atualizar e Excluir [cite: 27, 30]
    @PUT("pokemons/{id}")
    suspend fun updatePokemon(@Path("id") id: Int, @Body pokemon: Pokemon): Response<Void>

    @DELETE("pokemons/{id}")
    suspend fun deletePokemon(@Path("id") id: Int): Response<Void>
}