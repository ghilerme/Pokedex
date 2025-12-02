package com.example.pokedex

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CadastroActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etTipo: EditText
    private lateinit var etHab1: EditText
    private lateinit var etHab2: EditText
    private lateinit var etHab3: EditText
    private lateinit var btnCadastrar: Button
    private lateinit var ivPokemonImage: ImageView
    private lateinit var btnSelecionarImagem: Button

    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                ivPokemonImage.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        etNome = findViewById(R.id.etNome)
        etTipo = findViewById(R.id.etTipo)
        etHab1 = findViewById(R.id.etHab1)
        etHab2 = findViewById(R.id.etHab2)
        etHab3 = findViewById(R.id.etHab3)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        ivPokemonImage = findViewById(R.id.ivPokemonImage)
        btnSelecionarImagem = findViewById(R.id.btnSelecionarImagem)

        btnSelecionarImagem.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        btnCadastrar.setOnClickListener {
            validarECadastrar()
        }
    }

    private fun validarECadastrar() {
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

        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecione uma imagem para o Pokémon", Toast.LENGTH_SHORT).show()
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

        enviarCadastro(nome, tipo, listaHabilidades, usuarioLogado, selectedImageUri!!)
    }

    private fun enviarCadastro(nome: String, tipo: String, habilidades: List<String>, usuario: String, imageUri: Uri) {
        btnCadastrar.isEnabled = false

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Erro: Você não está logado.", Toast.LENGTH_SHORT).show()
            btnCadastrar.isEnabled = true
            return
        }

        val inputStream = contentResolver.openInputStream(imageUri)
        if (inputStream == null) {
            Toast.makeText(this, "Não foi possível ler a imagem selecionada.", Toast.LENGTH_SHORT).show()
            btnCadastrar.isEnabled = true
            return
        }

        val mimeType = contentResolver.getType(imageUri)
        var filename = "pokemon.jpg" // Fallback
        contentResolver.query(imageUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    filename = cursor.getString(index)
                }
            }
        }

        val nomeRB = nome.toRequestBody("text/plain".toMediaTypeOrNull())
        val tipoRB = tipo.toRequestBody("text/plain".toMediaTypeOrNull())
        val usuarioRB = usuario.toRequestBody("text/plain".toMediaTypeOrNull())
        val habilidadesString = habilidades.joinToString(",")
        val habilidadesRB = habilidadesString.toRequestBody("text/plain".toMediaTypeOrNull())

        val imageRequestBody = inputStream.readBytes().toRequestBody(mimeType?.toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("imagem", filename, imageRequestBody)


        lifecycleScope.launch {
            try {
                // Esta linha ainda vai dar erro, pois a API não foi atualizada.
                // Vou corrigir isso no próximo passo.
                val response = RetrofitClient.api.createPokemon(
                    token = "Bearer $token",
                    nome = nomeRB,
                    tipo = tipoRB,
                    habilidades = habilidadesRB,
                    usuario_cadastro = usuarioRB,
                    imagem = imagePart
                )

                if (response.isSuccessful) {
                    showDialog("Sucesso", "Pokémon cadastrado com sucesso!") {
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CADASTRO_ERRO", "Código: ${response.code()} - Body: $errorBody")
                    val errorMsg = if (response.code() == 409) {
                        "Já existe um Pokémon com este nome."
                    } else {
                        "Erro ao cadastrar: ${response.message()}"
                    }
                    showDialog("Erro", errorMsg, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showDialog("Erro de Conexão", "Não foi possível conectar ao servidor.", null)
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
