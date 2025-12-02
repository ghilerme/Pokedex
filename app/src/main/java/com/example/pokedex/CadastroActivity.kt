package com.example.pokedex

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pokedex.model.Pokemon
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CadastroActivity : AppCompatActivity() {

    private lateinit var etUrlImagem: TextInputEditText
    private lateinit var etNome: EditText
    private lateinit var etTipo: EditText
    private lateinit var etHab1: EditText
    private lateinit var etHab2: EditText
    private lateinit var etHab3: EditText
    private lateinit var btnCadastrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        etUrlImagem = findViewById(R.id.etUrlImagem)
        etNome = findViewById(R.id.etNome)
        etTipo = findViewById(R.id.etTipo)
        etHab1 = findViewById(R.id.etHab1)
        etHab2 = findViewById(R.id.etHab2)
        etHab3 = findViewById(R.id.etHab3)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            validarECadastrar()
        }
    }

    private fun validarECadastrar() {
        val urlImagem = etUrlImagem.text.toString().trim()
        val nome = etNome.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val hab1 = etHab1.text.toString().trim()
        val hab2 = etHab2.text.toString().trim()
        val hab3 = etHab3.text.toString().trim()

        if (nome.isEmpty()) {
            etNome.error = "Nome é obrigatório"
            return
        }
        if (tipo.isEmpty()) {
            etTipo.error = "Tipo é obrigatório"
            return
        }
        if (hab1.isEmpty()) {
            etHab1.error = "Pelo menos uma habilidade é obrigatória"
            return
        }

        // Validação da URL da imagem
        if (urlImagem.isNotEmpty() &&
            (!urlImagem.startsWith("http") ||
            !urlImagem.contains("://") ||
            !(
                urlImagem.endsWith(".png", ignoreCase = true) ||
                urlImagem.endsWith(".jpg", ignoreCase = true) ||
                urlImagem.endsWith(".jpeg", ignoreCase = true) ||
                urlImagem.endsWith(".webp", ignoreCase = true)
            ))
        ) {
            etUrlImagem.error = "URL inválida. Use um link direto para a imagem (.png, .jpg, etc.)"
            return
        }

        val listaHabilidades = mutableListOf<String>()
        listaHabilidades.add(hab1)
        if (hab2.isNotEmpty()) listaHabilidades.add(hab2)
        if (hab3.isNotEmpty()) listaHabilidades.add(hab3)

        val sessionManager = SessionManager(this)
        val usuarioLogado = sessionManager.getUserLogin()

        if (usuarioLogado == null) {
            Toast.makeText(this, "Erro: Usuário não logado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val novoPokemon = Pokemon(
            nome = nome,
            tipo = tipo,
            habilidades = listaHabilidades,
            imagemUrl = urlImagem,
            usuario_cadastro = usuarioLogado
        )

        enviarCadastro(novoPokemon)
    }

    private fun enviarCadastro(pokemon: Pokemon) {
        btnCadastrar.isEnabled = false

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Erro: Você não está logado.", Toast.LENGTH_SHORT).show()
            btnCadastrar.isEnabled = true
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.createPokemon("Bearer $token", pokemon)

                if (response.isSuccessful) {
                    showDialog("Sucesso", "Pokémon cadastrado com sucesso!", true)
                } else {
                    val errorMsg = if (response.code() == 409) {
                        "Já existe um Pokémon com este nome."
                    } else {
                        "Erro ao cadastrar: ${response.code()}"
                    }
                    showDialog("Erro", errorMsg, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showDialog("Erro de Conexão", "Não foi possível conectar ao servidor.", false)
            } finally {
                btnCadastrar.isEnabled = true
            }
        }
    }

    private fun showDialog(titulo: String, mensagem: String, finishOnOk: Boolean) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensagem)
            .setPositiveButton("OK") { _, _ ->
                if (finishOnOk) {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }
}
