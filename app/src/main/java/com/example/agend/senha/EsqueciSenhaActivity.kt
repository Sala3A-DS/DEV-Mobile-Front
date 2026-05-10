package com.example.agend.senha

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.senha.ConfirmarCodigoActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EsqueciSenhaActivity : AppCompatActivity() {

    // TAG usada para filtrar os logs no Logcat.
    private val TAG = "ESQUECI_SENHA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esqueci_senha)

        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmailEsqueci)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailEsqueci)
        val botaoEnviar = findViewById<Button>(R.id.botaoEnviarCodigo)
        val textoErro = findViewById<TextView>(R.id.textoErroEsqueci)
        val textoVoltar = findViewById<TextView>(R.id.textoVoltarEsqueci)

        botaoEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()

            // Limpa erros anteriores da tela.
            textoErro.visibility = View.GONE
            layoutEmail.error = null

            // Valida se o e-mail foi preenchido.
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            // Valida se o e-mail tem formato correto.
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            // Bloqueia o botão para evitar vários envios ao mesmo tempo.
            botaoEnviar.isEnabled = false
            botaoEnviar.text = "Enviando..."

            // Log para confirmar qual e-mail está sendo enviado ao back-end.
            Log.d(TAG, "Enviando solicitação de recuperação para: $email")

            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email))
                .enqueue(object : Callback<String> {

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        botaoEnviar.isEnabled = true
                        botaoEnviar.text = "Enviar Código"

                        // Lê o corpo de sucesso, se existir.
                        val respostaServidor = response.body() ?: ""

                        // Lê o corpo de erro, se existir.
                        val erroServidor = response.errorBody()?.string()

                        // Logs importantes para descobrir o retorno real do servidor.
                        Log.d(TAG, "Código HTTP: ${response.code()}")
                        Log.d(TAG, "Resposta sucesso: $respostaServidor")
                        Log.d(TAG, "Resposta erro: $erroServidor")

                        if (response.isSuccessful) {
                            // Verifica se o servidor retornou sucesso.
                            // ignoreCase evita erro caso venha "Sucesso", "sucesso", "SUCESSO", etc.
                            if (respostaServidor.contains("SUCESSO", ignoreCase = true)) {
                                val intent = Intent(
                                    this@EsqueciSenhaActivity,
                                    ConfirmarCodigoActivity::class.java
                                )

                                // Passa o e-mail para a próxima tela validar o código.
                                intent.putExtra("email", email)

                                startActivity(intent)
                            } else {
                                textoErro.text = if (respostaServidor.isNotBlank()) {
                                    "⚠️ $respostaServidor"
                                } else {
                                    "⚠️ Não foi possível enviar o código."
                                }

                                textoErro.visibility = View.VISIBLE
                            }
                        } else {
                            textoErro.text = if (!erroServidor.isNullOrBlank()) {
                                "⚠️ $erroServidor"
                            } else {
                                "⚠️ Erro no servidor: ${response.code()}"
                            }

                            textoErro.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        botaoEnviar.isEnabled = true
                        botaoEnviar.text = "Enviar Código"

                        // Log mais importante para descobrir o erro real.
                        // Aqui pode aparecer timeout, erro de DNS, SSL, conexão recusada, etc.
                        Log.e(TAG, "Falha na conexão com o servidor", t)

                        textoErro.text = "⚠️ Falha na conexão com o servidor."
                        textoErro.visibility = View.VISIBLE
                    }
                })
        }

        textoVoltar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}