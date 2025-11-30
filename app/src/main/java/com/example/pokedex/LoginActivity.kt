package com.example.pokedex

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pokedex.network.RetrofitClient
import com.example.pokedex.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etLogin: EditText
    private lateinit var etSenha: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        etLogin = findViewById(R.id.etLogin)
        etSenha = findViewById(R.id.etSenha)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val login = etLogin.text.toString().trim()
        val senha = etSenha.text.toString().trim()

        if (login.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val dadosLogin = mapOf("login" to login, "senha" to senha)

                val response = RetrofitClient.api.login(dadosLogin)

                if (response.isSuccessful) {
                    sessionManager.saveUserSession(login)

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    mostrarAlertaErro("Login ou Senha incorretos")
                }
            } catch (e: Exception) {
                mostrarAlertaErro("Erro de conexão: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }

    private fun mostrarAlertaErro(mensagem: String) {
        AlertDialog.Builder(this)
            .setTitle("Atenção")
            .setMessage(mensagem)
            .setPositiveButton("OK", null)
            .show()
    }
}