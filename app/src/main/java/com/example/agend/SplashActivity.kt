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

        // Inicializa a tela de Splash.
        setContentView(R.layout.activity_splash)

        // Gerencia os dados da sessão persistida do usuário.
        sessionManager = SessionManager(this)

        // Aguarda a exibição da Splash antes de restaurar a sessão.
        Handler(Looper.getMainLooper()).postDelayed({
            abrirProximaTela()
        }, 3000)
    }

    private fun abrirProximaTela() {
        // Sem sessão válida, direciona o usuário para o login.
        if (!sessionManager.estaLogado()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val token = sessionManager.getToken()

        // Token ausente ou inválido: encerra a sessão e retorna ao login.
        if (token.isNullOrBlank()) {
            sessionManager.limparSessao()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Disponibiliza o token para as requisições autenticadas do Retrofit.
        RetrofitClient.token = token

        val cargo = sessionManager.getCargo().trim().uppercase()

        // Define a tela inicial conforme o perfil de acesso.
        val intent = if (cargo == "ADM") {
            Intent(this, DiretorHomeActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        // Mantém os dados da sessão disponíveis na Activity de destino.
        intent.putExtra("email", sessionManager.getEmail())
        intent.putExtra("nome", sessionManager.getNome())
        intent.putExtra("cargo", sessionManager.getCargo())

        startActivity(intent)
        finish()
    }
}