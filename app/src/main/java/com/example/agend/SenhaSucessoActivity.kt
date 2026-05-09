package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SenhaSucessoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senha_sucesso)

        findViewById<Button>(R.id.botaoVoltarLogin).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Limpa toda a pilha de telas para não voltar ao fluxo de recuperação
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // Impede o usuário de voltar para as telas de recuperação com o botão "voltar"
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}