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
    private lateinit var etUrlImagem: TextInputEditText // <-- Novo campo
    private lateinit var ivPokemon: ImageView

    private lateinit var tilHab1: TextInputLayout
    private lateinit var tilHab2: TextInputLayout
    private lateinit var tilHab3: TextInputLayout

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
        recuperarDadosIntent()
    }

    private fun initViews() {
        etNome = findViewById(R.id.etNomeDetalhe)
        etTipo = findViewById(R.id.etTipoDetalhe)
        etUrlImagem = findViewById(R.id.etUrlImagemDetalhe) // <-- Bind do novo campo
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
        val urlImagem = intent.getStringExtra("POKEMON_URL_IMAGEM") ?: "" // <-- Recupera URL
        val listaHabilidades = intent.getStringArrayListExtra("POKEMON_HABILIDADES") ?: listOf()
        usuarioOriginal = intent.getStringExtra("POKEMON_USUARIO")

        preencherCampos(nome, tipo, urlImagem, listaHabilidades, usuarioOriginal)

        // Carrega a imagem no topo usando a URL recebida
        if (urlImagem.isNotEmpty()) {
            Glide.with(this)
                .load(urlImagem)
                .placeholder(R.drawable.logo_pokemon)
                .error(R.drawable.logo_pokemon)
                .into(ivPokemon)
        } else {
            // Fallback se não tiver URL
            ivPokemon.setImageResource(R.drawable.logo_pokemon)
        }
    }

    private fun preencherCampos(nome: String, tipo: String, urlImagem: String, listaHabilidades: List<String>, usuario: String?) {
        etNome.setText(nome)
        etTipo.setText(tipo)
        etUrlImagem.setText(urlImagem) // <-- Preenche o campo com a URL atual
        tvUsuarioCriador.text = "Criado por: ${usuario ?: "Desconhecido"}"

        // Lógica de habilidades (mostra os campos conforme necessário)
        tilHab1.visibility = View.VISIBLE // Sempre mostra pelo menos 1
        etHab1.setText(if (listaHabilidades.isNotEmpty()) listaHabilidades[0] else "")

        tilHab2.visibility = View.VISIBLE
        etHab2.setText(if (listaHabilidades.size >= 2) listaHabilidades[1] else "")

        tilHab3.visibility = View.VISIBLE
        etHab3.setText(if (listaHabilidades.size >= 3) listaHabilidades[2] else "")
    }

    private fun atualizarPokemon() {
        val nome = etNome.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val urlImagem = etUrlImagem.text.toString().trim() // <-- Pega o valor do campo (editado ou não)

        // Validações
        if (nome.isEmpty() || tipo.isEmpty() || urlImagem.isEmpty()) {
            Toast.makeText(this, "Nome, Tipo e URL da Imagem são obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação simples de URL
        if (!urlImagem.startsWith("http")) {
            Toast.makeText(this, "URL da imagem inválida (deve começar com http)", Toast.LENGTH_SHORT).show()
            return
        }

        val novasHabilidades = mutableListOf<String>()
        if (!etHab1.text.isNullOrEmpty()) novasHabilidades.add(etHab1.text.toString().trim())
        if (!etHab2.text.isNullOrEmpty()) novasHabilidades.add(etHab2.text.toString().trim())
        if (!etHab3.text.isNullOrEmpty()) novasHabilidades.add(etHab3.text.toString().trim())

        if (novasHabilidades.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma habilidade", Toast.LENGTH_SHORT).show()
            return
        }

        if (pokemonId == null) {
            Toast.makeText(this, "Erro: ID do Pokémon perdido", Toast.LENGTH_SHORT).show()
            return
        }

        // Objeto atualizado
        val pokemonAtualizado = Pokemon(
            id = pokemonId,
            nome = nome,
            tipo = tipo,
            habilidades = novasHabilidades,
            imagemUrl = urlImagem, // <-- Envia a URL (nova ou antiga)
            usuario_cadastro = usuarioOriginal
        )

        enviarAtualizacaoParaAPI(pokemonAtualizado)
    }

    private fun enviarAtualizacaoParaAPI(pokemon: Pokemon) {
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
                // Endpoint PUT requer imageUrl no body
                val response = RetrofitClient.api.updatePokemon(
                    "Bearer $token",
                    pokemon.id!!,
                    pokemon
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@DetalhesActivity, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = if (response.code() == 409) {
                        "Já existe um Pokémon com este nome."
                    } else {
                        "Erro ao salvar: ${response.code()}"
                    }
                    Toast.makeText(this@DetalhesActivity, errorMsg, Toast.LENGTH_LONG).show()
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
        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken() ?: return

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
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
                btnExcluir.isEnabled = true
            }
        }
    }
}
