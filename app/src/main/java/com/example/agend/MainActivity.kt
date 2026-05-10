package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.LoginRequest
import com.example.agend.auth.LoginResponse // ATUALIZADO: Importando a resposta com Token
import com.example.agend.auth.RetrofitClient // Certifique-se de importar o RetrofitClient
import com.example.agend.diretor.DiretorHomeActivity
import com.example.agend.professor.HomeActivity
import com.example.agend.senha.EsqueciSenhaActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val layoutEmail   = findViewById<TextInputLayout>(R.id.layoutEmail)
        val layoutSenha   = findViewById<TextInputLayout>(R.id.layoutSenha)
        val editEmail     = findViewById<TextInputEditText>(R.id.editEmail)
        val editSenha     = findViewById<TextInputEditText>(R.id.editSenha)
        val botaoEntrar   = findViewById<Button>(R.id.botaoEntrar)
        val textoCadastro = findViewById<TextView>(R.id.textoCadastro)
        val textoErro     = findViewById<TextView>(R.id.textoErroLogin)
        val textoEsqueci  = findViewById<TextView>(R.id.textoEsqueciSenha)

        botaoEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            // Limpa erros anteriores
            textoErro.visibility = View.GONE
            layoutEmail.error = null
            layoutSenha.error = null

            // 1. Validação: campos vazios
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            // 2. Validação: formato de e-mail real (ex: nome@dominio.com)
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                layoutSenha.error = "Informe a senha"
                return@setOnClickListener
            }

            // UI: Trava botão para evitar duplo clique
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Conectando..."

            val pedido = LoginRequest(email, senha)

            // ATUALIZADO: Agora esperamos receber um LoginResponse
            RetrofitClient.api.login(pedido).enqueue(object : Callback<LoginResponse> {

                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val usuario = loginResponse?.user

                        // Salva o token JWT para as próximas requisições protegidas.
                        RetrofitClient.token = loginResponse?.token

                        // Valida se a resposta veio completa.
                        if (loginResponse?.token.isNullOrBlank() || usuario == null) {
                            textoErro.text = "⚠️ Resposta inválida do servidor."
                            textoErro.visibility = View.VISIBLE
                            return
                        }

                        // Padroniza o cargo para evitar erro com letras minúsculas/maiúsculas.
                        val cargo = usuario.cargo.trim().uppercase()

                        // Se for ADM, abre a tela do diretor.
                        // Caso contrário, abre a Home normal de professor/coordenador.
                        val intent = if (cargo == "ADM") {
                            Intent(this@MainActivity, DiretorHomeActivity::class.java)
                        } else {
                            Intent(this@MainActivity, HomeActivity::class.java)
                        }

                        // Envia dados básicos do usuário para a próxima tela.
                        intent.putExtra("email", usuario.email)
                        intent.putExtra("nome", usuario.nome)
                        intent.putExtra("cargo", usuario.cargo)

                        startActivity(intent)
                        finish()
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
}