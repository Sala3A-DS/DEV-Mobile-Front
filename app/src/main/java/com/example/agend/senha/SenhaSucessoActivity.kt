package com.example.agend.senha

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R

class SenhaSucessoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_senha_sucesso)

        findViewById<Button>(R.id.botaoVoltarLogin).setOnClickListener {
            voltarParaLogin()
        }

        // Forma atual recomendada para tratar o botão "voltar" do Android.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    voltarParaLogin()
                }
            }
        )
    }

    private fun voltarParaLogin() {
        val intent = Intent(this, MainActivity::class.java)

        // Limpa toda a pilha de telas para não voltar ao fluxo de recuperação.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}