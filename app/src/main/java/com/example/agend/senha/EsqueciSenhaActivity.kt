package com.example.agend.senha

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.R
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher

class EsqueciSenhaActivity : AppCompatActivity() {

    private val TAG = "ESQUECI_SENHA"

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewAtual = currentFocus
            if (viewAtual is EditText) {
                val areaDoCampo = android.graphics.Rect()
                viewAtual.getGlobalVisibleRect(areaDoCampo)
                val tocouForaDoCampo = !areaDoCampo.contains(event.rawX.toInt(), event.rawY.toInt())
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
        setContentView(R.layout.activity_esqueci_senha)

        findViewById<LinearLayout>(R.id.layoutVoltarTopo).setOnClickListener { finish() }

        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmailEsqueci)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailEsqueci)
        val botaoEnviar = findViewById<Button>(R.id.botaoEnviarCodigo)
        val textoErro = findViewById<TextView>(R.id.textoErroEsqueci)

        editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutEmail.error = null
                textoErro.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        botaoEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            textoErro.visibility = View.GONE
            layoutEmail.error = null

            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido"
                return@setOnClickListener
            }

            botaoEnviar.isEnabled = false
            botaoEnviar.text = "Enviando..."

            Log.d(TAG, "Solicitando código para: $email")

            RetrofitClient.api.forgotPassword(ForgotPasswordRequest(email))
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        botaoEnviar.isEnabled = true
                        botaoEnviar.text = "Enviar Código"

                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@EsqueciSenhaActivity,
                                "Código enviado para seu e-mail.",
                                Toast.LENGTH_LONG
                            ).show()

                            val intent = Intent(this@EsqueciSenhaActivity, ConfirmarCodigoActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        } else {
                            val erroServidor = response.errorBody()?.string()
                            textoErro.text = if (!erroServidor.isNullOrBlank()) {
                                "⚠️ $erroServidor"
                            } else {
                                "⚠️ Não foi possível enviar o código."
                            }
                            textoErro.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        botaoEnviar.isEnabled = true
                        botaoEnviar.text = "Enviar Código"
                        Log.e(TAG, "Falha na conexão com o servidor", t)
                        textoErro.text = "⚠️ Falha na conexão com o servidor."
                        textoErro.visibility = View.VISIBLE
                    }
                })
        }
    }
}
