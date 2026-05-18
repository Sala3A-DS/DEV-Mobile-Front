package com.example.agend.senha

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher

class EsqueciSenhaActivity : AppCompatActivity() {

    //Sair da funcao do teclado
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewAtual = currentFocus

            // Se o foco atual estiver em um campo de texto, verifica se o toque foi fora dele.
            if (viewAtual is EditText) {
                val areaDoCampo = android.graphics.Rect()
                viewAtual.getGlobalVisibleRect(areaDoCampo)

                val tocouForaDoCampo = !areaDoCampo.contains(
                    event.rawX.toInt(),
                    event.rawY.toInt()
                )

                if (tocouForaDoCampo) {
                    // Remove o foco do campo.
                    viewAtual.clearFocus()

                    // Esconde o teclado.
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewAtual.windowToken, 0)
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

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

        // Limpa automaticamente o erro quando o usuário começa a corrigir o e-mail.
        // Isso faz o label voltar para a cor normal.
        editEmail.addTextChangedListener(object : TextWatcher {
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
                layoutEmail.error = null
                textoErro.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Não é necessário tratar depois da mudança.
            }
        })

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