package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.LoginRequest
import com.example.agend.auth.LoginResponse
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.DiretorHomeActivity
import com.example.agend.professor.HomeActivity
import com.example.agend.senha.EsqueciSenhaActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o gerenciador de sessão.
        // Ele salva e recupera token, e-mail, nome e cargo do usuário.
        sessionManager = SessionManager(this)

        // Se já existir uma sessão salva, redireciona direto para a tela correta.
        // Isso evita pedir login toda vez que o usuário abrir o app.
        verificarSessaoSalva()

        setContentView(R.layout.activity_login)

        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmail)
        val layoutSenha = findViewById<TextInputLayout>(R.id.layoutSenha)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenha)
        val botaoEntrar = findViewById<Button>(R.id.botaoEntrar)
        val textoCadastro = findViewById<TextView>(R.id.textoCadastro)
        val textoErro = findViewById<TextView>(R.id.textoErroLogin)
        val textoEsqueci = findViewById<TextView>(R.id.textoEsqueciSenha)

        botaoEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim().lowercase()
            val senha = editSenha.text.toString().trim()

            // Limpa erros anteriores.
            textoErro.visibility = View.GONE
            layoutEmail.error = null
            layoutSenha.error = null

            // Validação do e-mail.
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            // Validação da senha.
            if (senha.isEmpty()) {
                layoutSenha.error = "Informe a senha"
                return@setOnClickListener
            }

            // Trava o botão para evitar duplo clique.
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Conectando..."

            val pedido = LoginRequest(email, senha)

            RetrofitClient.api.login(pedido).enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val usuario = loginResponse?.user
                        val token = loginResponse?.token

                        // Valida se a resposta veio completa.
                        if (token.isNullOrBlank() || usuario == null) {
                            textoErro.text = "⚠️ Resposta inválida do servidor."
                            textoErro.visibility = View.VISIBLE
                            return
                        }

                        // Salva o token em memória para as próximas requisições enquanto o app está aberto.
                        RetrofitClient.token = token

                        // Salva a sessão no celular para manter o usuário logado ao reabrir o app.
                        sessionManager.salvarSessao(
                            token = token,
                            email = usuario.email,
                            nome = usuario.nome,
                            cargo = usuario.cargo
                        )

                        // Redireciona conforme o cargo.
                        abrirTelaPrincipal(
                            email = usuario.email,
                            nome = usuario.nome,
                            cargo = usuario.cargo
                        )
                    } else {
                        textoErro.text = "⚠️ E-mail ou senha incorretos."
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"

                    textoErro.text = "⚠️ Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        textoCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        textoEsqueci.setOnClickListener {
            startActivity(Intent(this, EsqueciSenhaActivity::class.java))
        }
    }

    private fun verificarSessaoSalva() {
        // Se não existe token salvo, continua na tela de login.
        if (!sessionManager.estaLogado()) {
            return
        }

        val token = sessionManager.getToken()

        if (token.isNullOrBlank()) {
            sessionManager.limparSessao()
            return
        }

        // Restaura o token no RetrofitClient.
        // Assim as próximas requisições já saem com Authorization Bearer.
        RetrofitClient.token = token

        abrirTelaPrincipal(
            email = sessionManager.getEmail(),
            nome = sessionManager.getNome(),
            cargo = sessionManager.getCargo()
        )
    }

    private fun abrirTelaPrincipal(
        email: String,
        nome: String,
        cargo: String
    ) {
        val cargoNormalizado = cargo.trim().uppercase()

        // ADM vai para o painel do diretor.
        // Qualquer outro cargo vai para a tela do professor/coordenador.
        val intent = if (cargoNormalizado == "ADM") {
            Intent(this, DiretorHomeActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        // Envia os dados básicos para a próxima tela.
        intent.putExtra("email", email)
        intent.putExtra("nome", nome)
        intent.putExtra("cargo", cargo)

        startActivity(intent)
        finish()
    }
}