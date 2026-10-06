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

        // Inicializa a tela principal do professor e coordenador.
        setContentView(R.layout.activity_home)

        val textoBemVindo = findViewById<TextView>(R.id.textoBemVindo)
        val textoNomeUsuario = findViewById<TextView>(R.id.textoNomeUsuario)
        val textoCargoUsuario = findViewById<TextView>(R.id.textoCargoUsuario)

        val botaoReservarSala = findViewById<Button>(R.id.botaoReservarSala)
        val botaoMinhasReservas = findViewById<Button>(R.id.botaoMinhasReservas)
        val botaoSair = findViewById<Button>(R.id.botaoSairProfessor)

        // Recupera os dados enviados pelo login ou pela sessão restaurada.
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

        // Abre a tela de reserva e encaminha os dados da sessão atual.
        botaoReservarSala.setOnClickListener {
            val intent = Intent(this, ReservarSalaActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("nome", nome)
            intent.putExtra("cargo", cargo)
            startActivity(intent)
        }

        // Abre a tela com as reservas realizadas pelo usuário.
        botaoMinhasReservas.setOnClickListener {
            val intent = Intent(this, MinhasReservasActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("nome", nome)
            intent.putExtra("cargo", cargo)
            startActivity(intent)
        }

        // Encerra a sessão atual e remove os dados de autenticação persistidos.
        botaoSair.setOnClickListener {
            RetrofitClient.token = null
            SessionManager(this).limparSessao()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        // Abre o diálogo com as informações e recursos do aplicativo.
        val botaoInfoApp = findViewById<TextView>(R.id.botaoInfoApp)

        botaoInfoApp.setOnClickListener {
            AppInfoDialog.mostrar(this)
        }
    }
}