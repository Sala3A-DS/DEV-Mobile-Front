package com.example.agend.diretor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.diretor.CadastrarSalaActivity
import com.example.agend.MainActivity
import com.example.agend.R

class DiretorHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela inicial do diretor/admin.
        setContentView(R.layout.activity_diretor_home)

        val textoNomeDiretor = findViewById<TextView>(R.id.textoNomeDiretor)
        val botaoCadastrarSala = findViewById<Button>(R.id.botaoCadastrarSala)
        val botaoMinhasSalas = findViewById<Button>(R.id.botaoMinhasSalas)
        val botaoSairDiretor = findViewById<Button>(R.id.botaoSairDiretor)

        // Recebe dados enviados pela tela de login.
        val nome = intent.getStringExtra("nome") ?: "Diretor"

        textoNomeDiretor.text = "Olá, $nome"

        // Abre a tela de cadastro de sala.
        botaoCadastrarSala.setOnClickListener {
            startActivity(Intent(this, CadastrarSalaActivity::class.java))
        }

        // Por enquanto, esse botão pode ficar preparado para a próxima etapa.
        // Depois criaremos uma tela para listar as salas cadastradas pelo diretor.
        botaoMinhasSalas.setOnClickListener {
            // Futuramente: startActivity(Intent(this, MinhasSalasActivity::class.java))
        }

        // Sai da área do diretor e volta para o login.
        botaoSairDiretor.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}