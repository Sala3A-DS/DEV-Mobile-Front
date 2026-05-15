package com.example.agend.senha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.R
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.VerifyCodeRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmarCodigoActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private val EXPIRACAO_MS = 1 * 60 * 1000L // 1 minuto.

    private lateinit var camposCodigo: List<EditText>

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewAtual = currentFocus

            // Fecha o teclado quando o usuário toca fora do campo.
            if (viewAtual is EditText) {
                val areaDoCampo = android.graphics.Rect()
                viewAtual.getGlobalVisibleRect(areaDoCampo)

                val tocouForaDoCampo = !areaDoCampo.contains(
                    event.rawX.toInt(),
                    event.rawY.toInt()
                )

                if (tocouForaDoCampo) {
                    viewAtual.clearFocus()

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewAtual.windowToken, 0)
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela de confirmação de código.
        setContentView(R.layout.activity_confirmar_codigo)

        val email = intent.getStringExtra("email") ?: ""

        val botaoConfirmar = findViewById<Button>(R.id.botaoConfirmarCodigo)
        val botaoReenviar = findViewById<TextView>(R.id.botaoReenviarCodigo)
        val textoErro = findViewById<TextView>(R.id.textoErroCodigo)
        val textoExpiracao = findViewById<TextView>(R.id.textoExpiracaoCodigo)
        val textoEmailEnv = findViewById<TextView>(R.id.textoEmailEnviado)

        // Campos separados do código.
        camposCodigo = listOf(
            findViewById(R.id.editCodigo1),
            findViewById(R.id.editCodigo2),
            findViewById(R.id.editCodigo3),
            findViewById(R.id.editCodigo4),
            findViewById(R.id.editCodigo5),
            findViewById(R.id.editCodigo6)
        )

        textoEmailEnv.text = "Enviamos um código para $email"

        configurarCamposCodigo()
        iniciarContagem(textoExpiracao)

        botaoConfirmar.setOnClickListener {
            val codigo = obterCodigoDigitado()
            textoErro.visibility = View.GONE

            if (codigo.length < 6) {
                textoErro.text = "⚠️ Digite o código completo."
                textoErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            botaoConfirmar.isEnabled = false
            botaoConfirmar.text = "Verificando..."

            RetrofitClient.api.verifyCode(VerifyCodeRequest(email, codigo)).enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoConfirmar.isEnabled = true
                    botaoConfirmar.text = "Confirmar Código"

                    if (response.isSuccessful) {
                        val respostaServidor = response.body() ?: ""

                        if (respostaServidor.contains("SUCESSO", ignoreCase = true)) {
                            countDownTimer?.cancel()

                            val intent = Intent(
                                this@ConfirmarCodigoActivity,
                                NovaSenhaActivity::class.java
                            )
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        } else {
                            textoErro.text = "⚠️ Código inválido ou expirado."
                            textoErro.visibility = View.VISIBLE
                        }
                    } else {
                        val erroServidor = response.errorBody()?.string()

                        textoErro.text = erroServidor ?: "⚠️ Código inválido ou expirado."
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

            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email)).enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoReenviar.isEnabled = true
                    botaoReenviar.text = "Não recebeu? Reenviar Código"

                    if (response.isSuccessful && response.body()?.contains("SUCESSO", ignoreCase = true) == true) {
                        limparCamposCodigo()

                        Toast.makeText(
                            this@ConfirmarCodigoActivity,
                            "✅ Código reenviado!",
                            Toast.LENGTH_SHORT
                        ).show()

                        iniciarContagem(textoExpiracao)
                    } else {
                        Toast.makeText(
                            this@ConfirmarCodigoActivity,
                            "⚠️ Erro ao reenviar código.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoReenviar.isEnabled = true
                    botaoReenviar.text = "Não recebeu? Reenviar Código"

                    Toast.makeText(
                        this@ConfirmarCodigoActivity,
                        "⚠️ Falha na conexão.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun configurarCamposCodigo() {
        camposCodigo.forEachIndexed { index, campo ->
            campo.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Não é necessário tratar antes da mudança.
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    // Não é necessário tratar durante a mudança.
                }

                override fun afterTextChanged(s: Editable?) {
                    // Ao digitar um número, avança automaticamente para o próximo campo.
                    if (!s.isNullOrEmpty() && index < camposCodigo.lastIndex) {
                        camposCodigo[index + 1].requestFocus()
                    }
                }
            })

            campo.setOnKeyListener { _, keyCode, event ->
                val apagou = keyCode == KeyEvent.KEYCODE_DEL &&
                        event.action == KeyEvent.ACTION_DOWN

                // Ao apagar em um campo vazio, volta para o campo anterior.
                if (apagou && campo.text.isNullOrEmpty() && index > 0) {
                    camposCodigo[index - 1].requestFocus()
                    camposCodigo[index - 1].setSelection(
                        camposCodigo[index - 1].text?.length ?: 0
                    )
                    true
                } else {
                    false
                }
            }
        }

        // Foca automaticamente no primeiro campo.
        camposCodigo.firstOrNull()?.requestFocus()
    }

    private fun obterCodigoDigitado(): String {
        // Junta os 6 campos em uma única String.
        return camposCodigo.joinToString(separator = "") {
            it.text.toString().trim()
        }
    }

    private fun limparCamposCodigo() {
        // Limpa todos os campos e volta o foco para o primeiro.
        camposCodigo.forEach { it.setText("") }
        camposCodigo.firstOrNull()?.requestFocus()
    }

    private fun iniciarContagem(textoExpiracao: TextView) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(EXPIRACAO_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60

                textoExpiracao.text = "⏱ O código expira em %02d:%02d".format(min, sec)

                textoExpiracao.setTextColor(
                    if (millisUntilFinished < 60_000) {
                        0xFFEF5350.toInt()
                    } else {
                        0xFFFFD54F.toInt()
                    }
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

        // Cancela o timer para evitar vazamento de memória.
        countDownTimer?.cancel()
    }
}