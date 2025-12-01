package com.example.pokedex

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.pokedex.model.Pokemon
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class DetalhesActivity : AppCompatActivity() {

    private lateinit var etNome: TextInputEditText
    private lateinit var etTipo: TextInputEditText
    private lateinit var ivPokemon: ImageView

    // Layouts para controlar visibilidade
    private lateinit var tilHab1: TextInputLayout
    private lateinit var tilHab2: TextInputLayout
    private lateinit var tilHab3: TextInputLayout

    // Campos de texto
    private lateinit var etHab1: TextInputEditText
    private lateinit var etHab2: TextInputEditText
    private lateinit var etHab3: TextInputEditText

    private lateinit var tvUsuarioCriador: TextView
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluir: Button
    private lateinit var progressBar: ProgressBar

    private var pokemonId: String? = null
    private var usuarioOriginal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        initViews()
        setupListeners()

        // RECUPERA OS DADOS ENVIADOS PELA INTENT
        recuperarDadosIntent()
    }

    private fun initViews() {
        etNome = findViewById(R.id.etNomeDetalhe)
        etTipo = findViewById(R.id.etTipoDetalhe)
        ivPokemon = findViewById(R.id.ivPokemonDetalhe)

        tilHab1 = findViewById(R.id.tilHabDetalhe1)
        tilHab2 = findViewById(R.id.tilHabDetalhe2)
        tilHab3 = findViewById(R.id.tilHabDetalhe3)

        etHab1 = findViewById(R.id.etHabDetalhe1)
        etHab2 = findViewById(R.id.etHabDetalhe2)
        etHab3 = findViewById(R.id.etHabDetalhe3)

        tvUsuarioCriador = findViewById(R.id.tvUsuarioCriador)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)
        progressBar = findViewById(R.id.progressBarDetalhe)
    }

    private fun setupListeners() {
        btnSalvar.setOnClickListener { atualizarPokemon() }
        btnExcluir.setOnClickListener { confirmarExclusao() }
    }

    private fun recuperarDadosIntent() {
        pokemonId = intent.getStringExtra("POKEMON_ID")
        val nome = intent.getStringExtra("POKEMON_NOME") ?: ""
        val tipo = intent.getStringExtra("POKEMON_TIPO") ?: ""
        val listaHabilidades = intent.getStringArrayListExtra("POKEMON_HABILIDADES") ?: listOf()
        usuarioOriginal = intent.getStringExtra("POKEMON_USUARIO")

        preencherCampos(nome, tipo, listaHabilidades, usuarioOriginal)

        // --- CORREÇÃO AQUI ---
        // Em vez de: ivPokemon.setImageResource(R.drawable.logo_pokemon)
        // Usamos o Glide com o nome do Pokémon (igual ao Adapter)
        
        if (nome.isNotEmpty()) {
            val imageUrl = "https://img.pokemondb.net/artwork/large/${nome.lowercase()}.jpg"

            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo_pokemon) // Mostra logo enquanto carrega
                .error(R.drawable.logo_pokemon)       // Mostra logo se der erro
                .into(ivPokemon)
        } else {
            ivPokemon.setImageResource(R.drawable.logo_pokemon)
        }
    }

    private fun preencherCampos(nome: String, tipo: String, listaHabilidades: List<String>, usuario: String?) {
        etNome.setText(nome)
        etTipo.setText(tipo)
        tvUsuarioCriador.text = usuario ?: "Desconhecido"

        tilHab1.visibility = View.GONE
        tilHab2.visibility = View.GONE
        tilHab3.visibility = View.GONE

        if (listaHabilidades.isNotEmpty()) {
            tilHab1.visibility = View.VISIBLE
            etHab1.setText(listaHabilidades[0])
        }

        if (listaHabilidades.size >= 2) {
            tilHab2.visibility = View.VISIBLE
            etHab2.setText(listaHabilidades[1])
        }

        if (listaHabilidades.size >= 3) {
            tilHab3.visibility = View.VISIBLE
            etHab3.setText(listaHabilidades[2])
        }

        if (listaHabilidades.isEmpty()) {
            tilHab1.visibility = View.VISIBLE
        }
    }

    private fun atualizarPokemon() {
        val nome = etNome.text.toString().trim()
        val tipo = etTipo.text.toString().trim()

        val novasHabilidades = mutableListOf<String>()

        if (tilHab1.visibility == View.VISIBLE && !etHab1.text.isNullOrEmpty()) {
            novasHabilidades.add(etHab1.text.toString().trim())
        }
        if (tilHab2.visibility == View.VISIBLE && !etHab2.text.isNullOrEmpty()) {
            novasHabilidades.add(etHab2.text.toString().trim())
        }
        if (tilHab3.visibility == View.VISIBLE && !etHab3.text.isNullOrEmpty()) {
            novasHabilidades.add(etHab3.text.toString().trim())
        }

        if (nome.isEmpty() || tipo.isEmpty() || novasHabilidades.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        if (pokemonId == null) {
            Toast.makeText(this, "ID do Pokémon não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria o objeto para envio
        val pokemonAtualizado = Pokemon(
            id = pokemonId,
            nome = nome,
            tipo = tipo,
            habilidades = novasHabilidades,
            usuario_cadastro = usuarioOriginal
        )

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Erro de autenticação", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSalvar.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.updatePokemon(
                    "Bearer $token",
                    pokemonId!!,
                    pokemonAtualizado
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@DetalhesActivity, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetalhesActivity, "Erro ao salvar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetalhesActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                btnSalvar.isEnabled = true
            }
        }
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Pokémon")
            .setMessage("Tem certeza que deseja excluir este Pokémon?")
            .setPositiveButton("Sim") { _, _ -> deletarPokemon() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deletarPokemon() {
        if (pokemonId == null) {
            Toast.makeText(this, "ID do Pokémon não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Erro de autenticação", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnExcluir.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.deletePokemon("Bearer $token", pokemonId!!)

                if (response.isSuccessful) {
                    Toast.makeText(this@DetalhesActivity, "Excluído com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@DetalhesActivity, "Erro ao excluir", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetalhesActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                btnExcluir.isEnabled = true
            }
        }
    }
}
