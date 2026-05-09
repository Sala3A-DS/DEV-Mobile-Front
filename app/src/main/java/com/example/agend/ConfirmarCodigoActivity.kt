package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.VerifyCodeRequest
import com.example.agend.auth.ForgotPasswordRequest // Importe correto
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmarCodigoActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private val EXPIRACAO_MS = 10 * 60 * 1000L // 10 minutos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmar_codigo)

        val email = intent.getStringExtra("email") ?: ""

        val layoutCodigo   = findViewById<TextInputLayout>(R.id.layoutCodigo)
        val editCodigo     = findViewById<TextInputEditText>(R.id.editCodigo)
        val botaoConfirmar = findViewById<Button>(R.id.botaoConfirmarCodigo)
        val botaoReenviar  = findViewById<TextView>(R.id.botaoReenviarCodigo)
        val textoErro      = findViewById<TextView>(R.id.textoErroCodigo)
        val textoExpiracao = findViewById<TextView>(R.id.textoExpiracaoCodigo)
        val textoEmailEnv  = findViewById<TextView>(R.id.textoEmailEnviado)
        val textoVoltar    = findViewById<TextView>(R.id.textoVoltarEsqueci)

        textoEmailEnv.text = "Enviamos um código para $email"

        iniciarContagem(textoExpiracao)

        botaoConfirmar.setOnClickListener {
            val codigo = editCodigo.text.toString().trim()
            textoErro.visibility = View.GONE
            layoutCodigo.error   = null

            if (codigo.isEmpty()) {
                layoutCodigo.error = "Digite o código recebido"
                return@setOnClickListener
            }
            if (codigo.length < 6) { // Ajustado para 6 dígitos (tamanho padrão de códigos de segurança)
                layoutCodigo.error = "Código inválido"
                return@setOnClickListener
            }

            botaoConfirmar.isEnabled = false
            botaoConfirmar.text = "Verificando..."

            RetrofitClient.api.verifyCode(VerifyCodeRequest(email, codigo)).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoConfirmar.isEnabled = true
                    botaoConfirmar.text = "Confirmar Código"

                    if (response.isSuccessful) {
                        val respostaServidor = response.body() ?: ""

                        // ATUALIZAÇÃO: Checando se o Back-end validou o código
                        if (respostaServidor.contains("SUCESSO")) {
                            countDownTimer?.cancel()
                            val intent = Intent(this@ConfirmarCodigoActivity, NovaSenhaActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        } else {
                            textoErro.text = "⚠️ Código inválido ou expirado."
                            textoErro.visibility = View.VISIBLE
                        }
                    } else {
                        textoErro.text = "⚠️ Erro no servidor: ${response.code()}"
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoConfirmar.isEnabled = true
                    botaoConfirmar.text = "Confirmar Código"
                    textoErro.text = "⚠️ Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        botaoReenviar.setOnClickListener {
            countDownTimer?.cancel()
            botaoReenviar.isEnabled = false
            botaoReenviar.text = "Reenviando..."

            // ATUALIZAÇÃO: Usando a rota correta e enviando apenas o e-mail
            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email)).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoReenviar.isEnabled = true
                    botaoReenviar.text = "Reenviar Código"

                    if (response.isSuccessful && response.body()?.contains("SUCESSO") == true) {
                        Toast.makeText(this@ConfirmarCodigoActivity, "✅ Código reenviado!", Toast.LENGTH_SHORT).show()
                        iniciarContagem(textoExpiracao)
                    } else {
                        Toast.makeText(this@ConfirmarCodigoActivity, "⚠️ Erro ao reenviar código.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoReenviar.isEnabled = true
                    botaoReenviar.text = "Reenviar Código"
                    Toast.makeText(this@ConfirmarCodigoActivity, "⚠️ Falha na conexão.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        textoVoltar.setOnClickListener {
            countDownTimer?.cancel()
            startActivity(Intent(this, MainActivity::class.java)) // Volta para o Login
            finish()
        }
    }

    private fun iniciarContagem(textoExpiracao: TextView) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(EXPIRACAO_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                textoExpiracao.text = "⏱ O código expira em %02d:%02d".format(min, sec)
                textoExpiracao.setTextColor(
                    if (millisUntilFinished < 60_000) 0xFFEF5350.toInt() else 0xFFFFD54F.toInt()
                )
            }
            override fun onFinish() {
                textoExpiracao.text = "⚠️ Código expirado! Reenvie um novo."
                textoExpiracao.setTextColor(0xFFEF5350.toInt())
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}