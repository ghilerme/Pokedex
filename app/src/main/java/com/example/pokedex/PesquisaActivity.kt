package com.example.pokedex

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.adapter.PokemonAdapter
import com.example.pokedex.model.Pokemon
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import kotlinx.coroutines.launch

class PesquisaActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnPesquisar: Button
    private lateinit var rvResultados: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSemResultados: TextView
    private lateinit var tvTitulo: TextView

    private lateinit var adapter: PokemonAdapter
    private var modoPesquisa: String = "TIPO" // Pode ser "TIPO" ou "HABILIDADE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pesquisa)

        // 1. Receber o modo via Intent (vindo do Dashboard)
        modoPesquisa = intent.getStringExtra("MODE") ?: "TIPO"

        initViews()
        setupUI()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        btnPesquisar = findViewById(R.id.btnPesquisar)
        rvResultados = findViewById(R.id.rvResultados)
        progressBar = findViewById(R.id.progressBar)
        tvSemResultados = findViewById(R.id.tvSemResultados)
        tvTitulo = findViewById(R.id.tvTituloPesquisa)
    }

    private fun setupUI() {
        // Configura Título e Hint baseados no modo
        if (modoPesquisa == "HABILIDADE") {
            tvTitulo.text = "Por Habilidade"
            etSearch.hint = "Ex: Voar, Choque..."
        } else {
            tvTitulo.text = "Por Tipo"
            etSearch.hint = "Ex: Fogo, Água..."
        }

        // Configura RecyclerView
        rvResultados.layoutManager = LinearLayoutManager(this)
        adapter = PokemonAdapter(emptyList()) { pokemon ->
            // Clique no item (Futuramente leva para Detalhes)
            Toast.makeText(this, "Selecionado: ${pokemon.nome}", Toast.LENGTH_SHORT).show()
        }
        rvResultados.adapter = adapter

        // Botão de Pesquisa
        btnPesquisar.setOnClickListener {
            realizarBusca()
        }
    }

    private fun realizarBusca() {
        val termo = etSearch.text.toString().trim()
        if (termo.isEmpty()) {
            etSearch.error = "Digite algo para buscar"
            return
        }

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        tvSemResultados.visibility = View.GONE
        rvResultados.visibility = View.GONE
        btnPesquisar.isEnabled = false

        lifecycleScope.launch {
            try {
                // CORREÇÃO: Lógica para enviar o parâmetro correto
                val typeParam = if (modoPesquisa == "TIPO") termo else null
                val abilityParam = if (modoPesquisa == "HABILIDADE") termo else null

                val response = RetrofitClient.api.searchPokemons(
                    token = "Bearer $token",
                    type = typeParam,
                    ability = abilityParam
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // CORREÇÃO: Acessar a lista dentro do objeto de resposta
                    if (body.success && body.pokemon.isNotEmpty()) {
                        adapter.updateList(body.pokemon)
                        rvResultados.visibility = View.VISIBLE
                    } else {
                        tvSemResultados.visibility = View.VISIBLE
                    }
                } else {
                    tvSemResultados.text = "Erro no servidor."
                    tvSemResultados.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                tvSemResultados.text = "Erro de conexão."
                tvSemResultados.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
                btnPesquisar.isEnabled = true
            }
        }
    }
}