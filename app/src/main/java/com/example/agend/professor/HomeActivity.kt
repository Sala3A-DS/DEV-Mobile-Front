package com.example.agend.professor

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

class HomeActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var nome: String
    private lateinit var cargo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tela principal do professor/coordenador.
        setContentView(R.layout.activity_home)

        val textoBemVindo = findViewById<TextView>(R.id.textoBemVindo)
        val textoNomeUsuario = findViewById<TextView>(R.id.textoNomeUsuario)
        val textoCargoUsuario = findViewById<TextView>(R.id.textoCargoUsuario)

        val botaoReservarSala = findViewById<Button>(R.id.botaoReservarSala)
        val botaoMinhasReservas = findViewById<Button>(R.id.botaoMinhasReservas)
        val botaoSair = findViewById<Button>(R.id.botaoSairProfessor)

        // Recebe os dados enviados pela tela de login ou pela sessão salva.
        email = intent.getStringExtra("email") ?: ""
        nome = intent.getStringExtra("nome") ?: "Usuário"
        cargo = intent.getStringExtra("cargo") ?: "PROFESSOR"

        textoBemVindo.text = "Bem-vindo,"
        textoNomeUsuario.text = nome
        textoCargoUsuario.text = when (cargo.uppercase()) {
            "COORDENADOR" -> "Coordenador(a)"
            "PROFESSOR" -> "Professor(a)"
            else -> "Usuário"
        }

        // Abre a tela de reserva de sala.
        botaoReservarSala.setOnClickListener {
            val intent = Intent(this, ReservarSalaActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("nome", nome)
            intent.putExtra("cargo", cargo)
            startActivity(intent)
        }

        // Abre a tela de reservas do professor.
        botaoMinhasReservas.setOnClickListener {
            val intent = Intent(this, MinhasReservasActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("nome", nome)
            intent.putExtra("cargo", cargo)
            startActivity(intent)
        }

        // Sai da conta, limpa o token em memória e remove a sessão salva no celular.
        botaoSair.setOnClickListener {
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