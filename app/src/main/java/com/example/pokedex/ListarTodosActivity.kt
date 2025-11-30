package com.example.pokedex

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
import kotlinx.coroutines.launch

class ListarTodosActivity : AppCompatActivity() {

    private lateinit var rvPokemons: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_todos)

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
            // Aqui tratamos o clique no item.
            // Conforme o requisito: "levando corretamente a tela de detalhes"
            // Como ainda não temos a tela Detalhes, deixei um Toast ou o Intent preparado
            Toast.makeText(this, "Detalhes de: ${pokemon.nome}", Toast.LENGTH_SHORT).show()

            // TODO: Descomentar quando a DetalhesActivity estiver criada
            /*
            val intent = Intent(this, DetalhesActivity::class.java)
            intent.putExtra("POKEMON_ID", pokemon.id)
            startActivity(intent)
            */
        }
        rvPokemons.adapter = adapter
    }

    private fun fetchPokemons() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                // Chama o método definido no seu PokemonApiService
                val response = RetrofitClient.api.getPokemons()

                if (response.isSuccessful) {
                    val listaPokemons = response.body()
                    if (!listaPokemons.isNullOrEmpty()) {
                        adapter.updateList(listaPokemons)
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

    // Atualiza a lista caso o usuário volte de outra tela (ex: editou um pokemon)
    override fun onResume() {
        super.onResume()
        fetchPokemons()
    }
}