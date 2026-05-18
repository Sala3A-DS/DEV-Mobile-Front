package com.example.agend.diretor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.utils.AppInfoDialog

class DiretorHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela inicial do diretor/admin.
        setContentView(R.layout.activity_diretor_home)

        val textoNomeDiretor = findViewById<TextView>(R.id.textoNomeDiretor)
        val botaoCadastrarSala = findViewById<Button>(R.id.botaoCadastrarSala)
        val botaoMinhasSalas = findViewById<Button>(R.id.botaoMinhasSalas)
        val botaoAgendamentosGerais = findViewById<Button>(R.id.botaoAgendamentosGerais)
        val botaoSairDiretor = findViewById<Button>(R.id.botaoSairDiretor)

        // Recebe dados enviados pela tela de login ou pela sessão salva.
        val nome = intent.getStringExtra("nome") ?: "Diretor"

        textoNomeDiretor.text = "Olá, $nome"

        // Abre a tela de cadastro de espaço/sala.
        botaoCadastrarSala.setOnClickListener {
            startActivity(Intent(this, CadastrarSalaActivity::class.java))
        }

        // Abre a tela com as salas cadastradas pelo diretor.
        botaoMinhasSalas.setOnClickListener {
            startActivity(Intent(this, MinhasSalasActivity::class.java))
        }

        // Abre a tela com todos os agendamentos feitos.
        botaoAgendamentosGerais.setOnClickListener {
            startActivity(Intent(this, AgendamentosGeraisActivity::class.java))
        }

        // Sai da conta, limpa o token em memória e remove a sessão salva no celular.
        botaoSairDiretor.setOnClickListener {
            RetrofitClient.token = null
            SessionManager(this).limparSessao()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Icone de Info
        val botaoInfoApp = findViewById<TextView>(R.id.botaoInfoApp)

        botaoInfoApp.setOnClickListener {
            AppInfoDialog.mostrar(this)
        }
    }
}