package com.example.agend

import androidx.core.view.ViewCompat
import com.example.agend.utils.AcessibilidadeUtils
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
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
import android.text.Editable
import android.text.TextWatcher

class MainActivity : AppCompatActivity() {

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

                    val imm = getSystemService(
                        Context.INPUT_METHOD_SERVICE
                    ) as InputMethodManager

                    imm.hideSoftInputFromWindow(viewAtual.windowToken, 0)
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o gerenciador responsável pela sessão persistida.
        sessionManager = SessionManager(this)

        setContentView(R.layout.activity_login)

        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmail)
        val layoutSenha = findViewById<TextInputLayout>(R.id.layoutSenha)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenha)
        val botaoEntrar = findViewById<Button>(R.id.botaoEntrar)
        val textoCadastro = findViewById<TextView>(R.id.textoCadastro)
        val textoErro = findViewById<TextView>(R.id.textoErroLogin)
        val textoEsqueci = findViewById<TextView>(R.id.textoEsqueciSenha)

        // Anuncia automaticamente as mensagens de erro do login.
        ViewCompat.setAccessibilityLiveRegion(
            textoErro,
            ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
        )

        // Anuncia os textos clicáveis como botões.
        AcessibilidadeUtils.marcarComoBotao(textoCadastro)
        AcessibilidadeUtils.marcarComoBotao(textoEsqueci)

        // Define o título da tela para navegação por títulos no TalkBack.
        ViewCompat.setAccessibilityHeading(
            findViewById<TextView>(R.id.textoTituloLogin),
            true
        )

        // Limpa o estado de erro enquanto o usuário corrige o e-mail.
        editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                layoutEmail.error = null
                textoErro.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Limpa o estado de erro enquanto o usuário corrige a senha.
        editSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                layoutSenha.error = null
                textoErro.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        botaoEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim().lowercase()
            val senha = editSenha.text.toString().trim()

            // Reseta mensagens de validação antes de uma nova tentativa.
            textoErro.visibility = View.GONE
            layoutEmail.error = null
            layoutSenha.error = null

            // Valida o preenchimento e o formato do e-mail.
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            // Valida o preenchimento da senha.
            if (senha.isEmpty()) {
                layoutSenha.error = "Informe a senha"
                return@setOnClickListener
            }

            // Evita múltiplas requisições durante o processamento do login.
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Conectando..."

            val pedido = LoginRequest(email, senha)

            // Envia as credenciais para autenticação no backend.
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

                        // Garante que a resposta contenha os dados necessários para criar a sessão.
                        if (token.isNullOrBlank() || usuario == null) {
                            textoErro.text = "⚠️ Resposta inválida do servidor."
                            textoErro.visibility = View.VISIBLE
                            return
                        }

                        // Mantém o token disponível para as requisições da sessão atual.
                        RetrofitClient.token = token

                        // Persiste os dados necessários para restaurar a sessão posteriormente.
                        sessionManager.salvarSessao(
                            token = token,
                            email = usuario.email,
                            nome = usuario.nome,
                            cargo = usuario.cargo
                        )

                        // Direciona o usuário para a área correspondente ao seu cargo.
                        abrirTelaPrincipal(
                            email = usuario.email,
                            nome = usuario.nome,
                            cargo = usuario.cargo
                        )
                    } else {
                        // Credenciais rejeitadas pelo backend.
                        textoErro.text = "⚠️ E-mail ou senha incorretos."
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"

                    // Diferencia timeout de outras falhas de comunicação.
                    textoErro.text =
                        if (t is java.net.SocketTimeoutException) {
                            "⚠️ O servidor demorou para responder. Tente novamente."
                        } else {
                            "⚠️ Falha na conexão com o servidor."
                        }

                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        // Abre a tela de cadastro de usuário.
        textoCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        // Abre o fluxo de recuperação de senha.
        textoEsqueci.setOnClickListener {
            startActivity(Intent(this, EsqueciSenhaActivity::class.java))
        }
    }

    private fun abrirTelaPrincipal(
        email: String,
        nome: String,
        cargo: String
    ) {
        val cargoNormalizado = cargo.trim().uppercase()

        // Define a tela inicial conforme o perfil de acesso.
        val intent = if (cargoNormalizado == "ADM") {
            Intent(this, DiretorHomeActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }

        // Encaminha os dados básicos do usuário para a Activity de destino.
        intent.putExtra("email", email)
        intent.putExtra("nome", nome)
        intent.putExtra("cargo", cargo)

        startActivity(intent)
        finish()
    }
}
