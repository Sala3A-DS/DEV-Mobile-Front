package com.example.agend.senha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.R
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EsqueciSenhaActivity : AppCompatActivity() {

    // Remove o foco e oculta o teclado ao tocar fora de um campo de texto.
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewAtual = currentFocus

            if (viewAtual is EditText) {
                val areaDoCampo = android.graphics.Rect()
                viewAtual.getGlobalVisibleRect(areaDoCampo)

                val tocouForaDoCampo = !areaDoCampo.contains(
                    event.rawX.toInt(),
                    event.rawY.toInt()
                )

                if (tocouForaDoCampo) {
                    viewAtual.clearFocus()

                    val imm =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                    imm.hideSoftInputFromWindow(viewAtual.windowToken, 0)
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    // Identifica os logs desta Activity no Logcat.
    private val TAG = "ESQUECI_SENHA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de recuperação de senha.
        setContentView(R.layout.activity_esqueci_senha)

        // Configura o botão de retorno para fechar a tela atual.
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmailEsqueci)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailEsqueci)
        val botaoEnviar = findViewById<Button>(R.id.botaoEnviarCodigo)
        val textoErro = findViewById<TextView>(R.id.textoErroEsqueci)

        // Remove os erros da tela enquanto o usuário corrige o e-mail.
        editEmail.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Nenhuma ação necessária antes da alteração.
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                layoutEmail.error = null
                textoErro.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Nenhuma ação necessária após a alteração.
            }
        })

        botaoEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()

            // Limpa os erros exibidos antes de iniciar uma nova validação.
            textoErro.visibility = View.GONE
            layoutEmail.error = null

            // Valida se o campo de e-mail foi preenchido.
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            // Valida o formato do e-mail informado.
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            // Evita múltiplas solicitações enquanto o envio está em andamento.
            botaoEnviar.isEnabled = false
            botaoEnviar.text = "Enviando..."

            // Registra no Logcat o início da solicitação de recuperação.
            Log.d(TAG, "Enviando solicitação de recuperação para: $email")

            // Solicita ao backend o envio do código de recuperação.
            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email))
                .enqueue(object : Callback<String> {

                    override fun onResponse(
                        call: Call<String>,
                        response: Response<String>
                    ) {
                        botaoEnviar.isEnabled = true
                        botaoEnviar.text = "Enviar Código"

                        // Obtém as respostas de sucesso e erro retornadas pelo servidor.
                        val respostaServidor = response.body() ?: ""
                        val erroServidor = response.errorBody()?.string()

                        // Registra no Logcat os dados da resposta para facilitar a análise.
                        Log.d(TAG, "Código HTTP: ${response.code()}")
                        Log.d(TAG, "Resposta sucesso: $respostaServidor")
                        Log.d(TAG, "Resposta erro: $erroServidor")

                        if (response.isSuccessful) {

                            // Confirma se o backend informou que o código foi enviado.
                            if (respostaServidor.contains("SUCESSO", ignoreCase = true)) {
                                val intent = Intent(
                                    this@EsqueciSenhaActivity,
                                    ConfirmarCodigoActivity::class.java
                                )

                                // Encaminha o e-mail para a validação do código na próxima tela.
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

                    override fun onFailure(
                        call: Call<String>,
                        t: Throwable
                    ) {
                        botaoEnviar.isEnabled = true
                        botaoEnviar.text = "Enviar Código"

                        // Registra a exceção completa para identificar a causa da falha.
                        Log.e(TAG, "Falha na conexão com o servidor", t)

                        textoErro.text = "⚠️ Falha na conexão com o servidor."
                        textoErro.visibility = View.VISIBLE
                    }
                })
        }
    }
}