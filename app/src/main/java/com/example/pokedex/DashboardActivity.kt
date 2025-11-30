package com.example.pokedex

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregarDadosDashboard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cadastrar -> {
                // Futuro: Intent(this, CadastroActivity::class.java)
                Toast.makeText(this, "Ir para Cadastro", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_listar -> {
                // Futuro: Intent(this, ListaActivity::class.java)
                Toast.makeText(this, "Ir para Lista", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_pesquisar_tipo -> {
                // Futuro: Intent(this, PesquisaTipoActivity::class.java)
                Toast.makeText(this, "Ir para Pesquisa Tipo", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_pesquisar_habilidade -> {
                // Futuro: Intent(this, PesquisaHabilidadeActivity::class.java)
                Toast.makeText(this, "Ir para Pesquisa Habilidade", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_sair -> {
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun carregarDadosDashboard() {
        val tvTotal = findViewById<TextView>(R.id.tvTotalPokemons)
        val tvTopTipos = findViewById<TextView>(R.id.tvTopTipos)
        val tvTopHabilidades = findViewById<TextView>(R.id.tvTopHabilidades)

        // --- SIMULAÇÃO DA API ---
        // Futuramente, aqui entrará o código do Retrofit para buscar do servidor.
        // Por enquanto, colocamos dados fixos para testar o layout.

        tvTotal.text = "15" // Exemplo: 15 cadastrados

        tvTopTipos.text = "1. Fogo\n2. Água\n3. Elétrico"

        tvTopHabilidades.text = "1. Overgrow\n2. Blaze\n3. Static"
    }
}