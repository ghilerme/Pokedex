package com.example.pokedex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pokedex.model.DashboardResponse
import com.example.pokedex.model.DashboardStats
import com.example.pokedex.network.RetrofitClient
import kotlinx.coroutines.launch
import com.example.pokedex.util.SessionManager


class DashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {

        sessionManager = SessionManager(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMenu = findViewById<ImageButton>(R.id.btnMenuHamburguer)
        btnMenu.setOnClickListener { view ->
            mostrarMenu(view)
        }

        carregarDadosDashboard()
    }

    private fun mostrarMenu(view: android.view.View) {

        val contextWrapper = ContextThemeWrapper(this, R.style.TemaWrapperMenu)
        val popup = PopupMenu(contextWrapper, view)

        popup.menuInflater.inflate(R.menu.menu_dashboard, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_cadastrar -> {
                    val intent = Intent(this, CadastroActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.action_listar -> {
                    val intent = Intent(this, ListarTodosActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.action_pesquisar_tipo -> {
                    val intent = Intent(this, PesquisaActivity::class.java)
                    intent.putExtra("MODE", "TIPO")
                    startActivity(intent)
                    true
                }

                R.id.action_pesquisar_habilidade -> {
                    val intent = Intent(this, PesquisaActivity::class.java)
                    intent.putExtra("MODE", "HABILIDADE")
                    startActivity(intent)
                    true
                }

                R.id.action_sair -> {
                    finishAffinity()
                    true
                }

                else -> false
            }
        }
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popup)
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.show()
    }

    private fun carregarDadosDashboard() {
        val tvTotal = findViewById<TextView>(R.id.tvTotalPokemons)
        val tvTopTipos = findViewById<TextView>(R.id.tvTopTipos)
        val tvTopHabilidades = findViewById<TextView>(R.id.tvTopHabilidades)

        val sessionManager = com.example.pokedex.util.SessionManager(this)
        val token = sessionManager.getToken()

        if (token == null) {
            Toast.makeText(this, "Você precisa logar para ver dados reais!", Toast.LENGTH_LONG)
                .show()
            tvTotal.text = "0"
            tvTopTipos.text = "Sem conexão"
            tvTopHabilidades.text = "Sem conexão"
            return
        }

        // 2. Conectar com a API
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDashboardStats("Bearer $token")

                if (response.isSuccessful) {
                    val dashboardResponse = response.body()

                    if (dashboardResponse != null && dashboardResponse.success) {
                        val stats = dashboardResponse.stats

                        tvTotal.text = stats.total.toString()

                        tvTopTipos.text = stats.topTypes.mapIndexed { index, item ->
                            "${index + 1}. ${item.name}: ${item.count}"
                        }.joinToString("\n")

                        tvTopHabilidades.text = stats.topAbilities.mapIndexed { index, item ->
                            "${index + 1}. ${item.name}: ${item.count}"
                        }.joinToString("\n")

                    }
                } else {
                    Toast.makeText(
                        this@DashboardActivity,
                        "Erro: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@DashboardActivity, "Erro de Conexão", Toast.LENGTH_SHORT).show()
            }
        }
    }
}