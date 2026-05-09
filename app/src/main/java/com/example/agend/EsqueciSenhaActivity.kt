package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EsqueciSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esqueci_senha)

        val layoutEmail    = findViewById<TextInputLayout>(R.id.layoutEmailEsqueci)
        val editEmail      = findViewById<TextInputEditText>(R.id.editEmailEsqueci)
        val botaoEnviar    = findViewById<Button>(R.id.botaoEnviarCodigo)
        val textoErro      = findViewById<TextView>(R.id.textoErroEsqueci)
        val textoVoltar    = findViewById<TextView>(R.id.textoVoltarEsqueci)

        botaoEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            textoErro.visibility = View.GONE
            layoutEmail.error    = null

            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            botaoEnviar.isEnabled = false
            botaoEnviar.text = "Enviando..."

            // ATUALIZADO: Usando o nome correto da função (forgotPassword) e passando só o E-mail
            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email)).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoEnviar.isEnabled = true
                    botaoEnviar.text = "Enviar Código"

                    if (response.isSuccessful) {
                        val respostaServidor = response.body() ?: ""

                        // ATUALIZADO: Verificando se o servidor conseguiu enviar o e-mail
                        if (respostaServidor.contains("SUCESSO")) {
                            // Passa o email para a próxima tela
                            val intent = Intent(this@EsqueciSenhaActivity, ConfirmarCodigoActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        } else {
                            // O servidor devolve "ERRO: Usuário não encontrado"
                            textoErro.text = "⚠️ $respostaServidor"
                            textoErro.visibility = View.VISIBLE
                        }
                    } else {
                        textoErro.text = "⚠️ Erro no servidor: ${response.code()}"
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoEnviar.isEnabled = true
                    botaoEnviar.text = "Enviar Código"
                    textoErro.text = "⚠️ Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        textoVoltar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)) // MainActivity costuma ser o Login
            finish()
        }
    }
}