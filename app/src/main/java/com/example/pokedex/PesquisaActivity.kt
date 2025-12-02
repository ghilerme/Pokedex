package com.example.pokedex

import android.content.Intent
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
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class PesquisaActivity : AppCompatActivity() {

    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: EditText
    private lateinit var btnPesquisar: Button
    private lateinit var rvResultados: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSemResultados: TextView
    private lateinit var tvTitulo: TextView

    private lateinit var adapter: PokemonAdapter
    private var modoPesquisa: String = "TIPO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pesquisa)

        modoPesquisa = intent.getStringExtra("MODE") ?: "TIPO"

        initViews()
        setupUI()
    }

    private fun initViews() {
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        btnPesquisar = findViewById(R.id.btnPesquisar)
        rvResultados = findViewById(R.id.rvResultados)
        progressBar = findViewById(R.id.progressBar)
        tvSemResultados = findViewById(R.id.tvSemResultados)
        tvTitulo = findViewById(R.id.tvTituloPesquisa)
    }

    private fun setupUI() {
        // CORREÇÃO: Removemos o "Digite aqui".
        // O hint do layout recebe diretamente o exemplo.
        if (modoPesquisa == "HABILIDADE") {
            tvTitulo.text = "Por Habilidade"
            tilSearch.hint = "Ex: Voar, Choque..."
        } else {
            tvTitulo.text = "Por Tipo"
            tilSearch.hint = "Ex: Fogo, Água..."
        }

        rvResultados.layoutManager = LinearLayoutManager(this)

        adapter = PokemonAdapter(emptyList()) { pokemon ->
            val intent = Intent(this, DetalhesActivity::class.java)
            intent.putExtra("POKEMON_ID", pokemon.id)
            intent.putExtra("POKEMON_NOME", pokemon.nome)
            intent.putExtra("POKEMON_TIPO", pokemon.tipo)
            intent.putStringArrayListExtra("POKEMON_HABILIDADES", ArrayList(pokemon.habilidades))
            intent.putExtra("POKEMON_USUARIO", pokemon.usuario_cadastro)
            intent.putExtra("POKEMON_URL_IMAGEM", pokemon.urlImagem)
            startActivity(intent)
        }

        rvResultados.adapter = adapter

        btnPesquisar.setOnClickListener {
            realizarBusca()
        }
    }

    private fun realizarBusca() {
        val termo = etSearch.text.toString().trim()

        if (termo.isEmpty()) {
            tilSearch.error = "Campo obrigatório"
            return
        } else {
            tilSearch.error = null
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
                val typeParam = if (modoPesquisa == "TIPO") termo else null
                val abilityParam = if (modoPesquisa == "HABILIDADE") termo else null

                val response = RetrofitClient.api.searchPokemons(
                    token = "Bearer $token",
                    type = typeParam,
                    ability = abilityParam
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success && body.pokemon.isNotEmpty()) {
                        adapter.updateList(body.pokemon)
                        rvResultados.visibility = View.VISIBLE
                    } else {
                        tvSemResultados.visibility = View.VISIBLE
                        tvSemResultados.text = "Nenhum Pokémon encontrado."
                    }
                } else {
                    tvSemResultados.text = "Erro no servidor: ${response.code()}"
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