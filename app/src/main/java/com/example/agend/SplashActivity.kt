package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.DiretorHomeActivity
import com.example.agend.professor.HomeActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Primeira tela do app.
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        // Aguarda 3 segundos antes de decidir para onde o usuário vai.
        Handler(Looper.getMainLooper()).postDelayed({
            abrirProximaTela()
        }, 3000)
    }

    private fun abrirProximaTela() {
        // Se não houver sessão salva, vai para o login.
        if (!sessionManager.estaLogado()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val token = sessionManager.getToken()

        // Se o token salvo estiver inválido/vazio, limpa a sessão e volta ao login.
        if (token.isNullOrBlank()) {
            sessionManager.limparSessao()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Restaura o token para o Retrofit enviar nas requisições protegidas.
        RetrofitClient.token = token

        val cargo = sessionManager.getCargo().trim().uppercase()

        val intent = if (cargo == "ADM") {
            Intent(this, DiretorHomeActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        // Envia os dados salvos para a próxima tela.
        intent.putExtra("email", sessionManager.getEmail())
        intent.putExtra("nome", sessionManager.getNome())
        intent.putExtra("cargo", sessionManager.getCargo())

        startActivity(intent)
        finish()
    }
}