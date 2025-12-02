package com.example.pokedex

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pokedex.model.Pokemon
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import kotlinx.coroutines.launch

class CadastroActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etTipo: EditText
    private lateinit var etUrlImagem: EditText // Novo campo para URL
    private lateinit var etHab1: EditText
    private lateinit var etHab2: EditText
    private lateinit var etHab3: EditText
    private lateinit var btnCadastrar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // Vinculação dos campos da UI
        etNome = findViewById(R.id.etNome)
        etTipo = findViewById(R.id.etTipo)
        etUrlImagem = findViewById(R.id.etUrlImagem) // Certifique-se de que este ID existe no XML
        etHab1 = findViewById(R.id.etHab1)
        etHab2 = findViewById(R.id.etHab2)
        etHab3 = findViewById(R.id.etHab3)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            validarECadastrar()
        }
    }

    private fun validarECadastrar() {
        val nome = etNome.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val urlImagem = etUrlImagem.text.toString().trim()
        val hab1 = etHab1.text.toString().trim()
        val hab2 = etHab2.text.toString().trim()
        val hab3 = etHab3.text.toString().trim()

        // Validações básicas
        if (nome.isEmpty()) {
            etNome.error = "Nome é obrigatório"
            return
        }
        if (urlImagem.isEmpty()) {
            etUrlImagem.error = "URL da imagem é obrigatória"
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

        // Monta a lista de habilidades
        val listaHabilidades = mutableListOf<String>()
        listaHabilidades.add(hab1)
        if (hab2.isNotEmpty()) listaHabilidades.add(hab2)
        if (hab3.isNotEmpty()) listaHabilidades.add(hab3)

        // Verifica sessão do usuário
        val sessionManager = SessionManager(this)
        val usuarioLogado = sessionManager.getUserLogin()

        if (usuarioLogado == null) {
            Toast.makeText(this, "Erro: Usuário não logado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        enviarCadastro(nome, tipo, listaHabilidades, usuarioLogado, urlImagem)
    }

    private fun enviarCadastro(
        nome: String,
        tipo: String,
        habilidades: List<String>,
        usuario: String,
        urlImagem: String
    ) {
        btnCadastrar.isEnabled = false

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Erro: Você não está logado.", Toast.LENGTH_SHORT).show()
            btnCadastrar.isEnabled = true
            return
        }

        // Cria o objeto Pokemon para enviar como JSON
        val pokemonParaCadastrar = Pokemon(
            nome = nome,
            tipo = tipo,
            habilidades = habilidades,
            urlImagem = urlImagem,
            usuario_cadastro = usuario
        )

        lifecycleScope.launch {
            try {
                // Chama o endpoint POST enviando o JSON no corpo (@Body)
                val response = RetrofitClient.api.createPokemon(
                    token = "Bearer $token",
                    pokemon = pokemonParaCadastrar
                )

                if (response.isSuccessful) {
                    showDialog("Sucesso", "Pokémon cadastrado com sucesso!") {
                        finish() // Fecha a activity após sucesso
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CADASTRO_ERRO", "Código: ${response.code()} - Body: $errorBody")

                    val errorMsg = when (response.code()) {
                        409 -> "Já existe um Pokémon com este nome."
                        400 -> "Dados inválidos. Verifique se a URL da imagem é válida."
                        else -> "Erro ao cadastrar: ${response.message()}"
                    }
                    showDialog("Erro", errorMsg, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showDialog("Erro de Conexão", "Não foi possível conectar ao servidor: ${e.message}", null)
            } finally {
                btnCadastrar.isEnabled = true
            }
        }
    }

    private fun showDialog(titulo: String, mensagem: String, onOk: (() -> Unit)?) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensagem)
            .setPositiveButton("OK") { _, _ ->
                onOk?.invoke()
            }
            .show()
    }
}