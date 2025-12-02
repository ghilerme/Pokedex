package com.example.pokedex

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.adapter.PokemonAdapter
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import kotlinx.coroutines.launch

class ListarTodosActivity : AppCompatActivity() {

    private lateinit var rvPokemons: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: PokemonAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_todos)

        sessionManager = SessionManager(this) // Inicializa

        initViews()
        setupRecyclerView()
        fetchPokemons()
    }

    private fun initViews() {
        rvPokemons = findViewById(R.id.rvPokemons)
        progressBar = findViewById(R.id.progressBarLista)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupRecyclerView() {
        rvPokemons.layoutManager = LinearLayoutManager(this)

        adapter = PokemonAdapter(emptyList()) { pokemon ->
            val intent = Intent(this, DetalhesActivity::class.java)
            
            intent.putExtra("POKEMON_ID", pokemon.id) // Agora passa String
            intent.putExtra("POKEMON_NOME", pokemon.nome)
            intent.putExtra("POKEMON_TIPO", pokemon.tipo)
            intent.putStringArrayListExtra("POKEMON_HABILIDADES", ArrayList(pokemon.habilidades))
            intent.putExtra("POKEMON_USUARIO", pokemon.usuario_cadastro)
            intent.putExtra("POKEMON_URL_IMAGEM", pokemon.imagemUrl)
            startActivity(intent)
        }
        rvPokemons.adapter = adapter
    }

    private fun fetchPokemons() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        // Recupera o token
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                // Passa o token para a API (adicionei "Bearer " por padrão, ajuste se necessário)
                val response = RetrofitClient.api.getPokemons("Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()

                    // CORREÇÃO: Verifica se body não é nulo e acessa a lista interna 'pokemon'
                    if (body != null && body.success && body.pokemon.isNotEmpty()) {
                        adapter.updateList(body.pokemon)
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@ListarTodosActivity, "Erro ao carregar lista.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ListarTodosActivity, "Erro de conexão.", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recarrega a lista ao voltar (caso tenha editado/excluído um item)
        fetchPokemons()
    }
}
