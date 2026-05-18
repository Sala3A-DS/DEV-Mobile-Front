package com.example.agend.senha

import com.example.agend.MainActivity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.agend.R
import com.example.agend.auth.ResetPasswordRequest
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

class NovaSenhaActivity : AppCompatActivity() {

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

    private fun configurarLimpezaErro(
        editText: TextInputEditText,
        layout: TextInputLayout,
        textoErro: TextView
    ) {
        editText.addTextChangedListener(object : TextWatcher {
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
                // Remove o erro visual quando o usuário começa a corrigir o campo.
                // Isso faz o label voltar para a cor normal.
                layout.error = null
                textoErro.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Não é necessário tratar depois da mudança.
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_senha)

        val email = intent.getStringExtra("email") ?: ""
        // O código recebido da tela anterior não é mais necessário aqui na requisição final

        val layoutNova      = findViewById<TextInputLayout>(R.id.layoutNovaSenha)
        val layoutConfirmar = findViewById<TextInputLayout>(R.id.layoutConfirmarSenha)
        val editNova        = findViewById<TextInputEditText>(R.id.editNovaSenha)
        val editConfirmar   = findViewById<TextInputEditText>(R.id.editConfirmarSenha)
        val botaoSalvar     = findViewById<Button>(R.id.botaoSalvarSenha)
        val textoErro       = findViewById<TextView>(R.id.textoErroNovaSenha)

        // Limpa automaticamente os erros dos campos quando o usuário digita.
        // Isso evita que o label continue vermelho depois que o texto foi corrigido.
        configurarLimpezaErro(editNova, layoutNova, textoErro)
        configurarLimpezaErro(editConfirmar, layoutConfirmar, textoErro)

        val layoutForca  = findViewById<View>(R.id.layoutForcaSenha) as LinearLayout
        val barra1       = findViewById<View>(R.id.barra1)
        val barra2       = findViewById<View>(R.id.barra2)
        val barra3       = findViewById<View>(R.id.barra3)
        val barra4       = findViewById<View>(R.id.barra4)
        val textoForca   = findViewById<TextView>(R.id.textoForcaSenha)
        val reqMinimo    = findViewById<TextView>(R.id.reqMinimo)
        val reqMaiuscula = findViewById<TextView>(R.id.reqMaiuscula)
        val reqMinuscula = findViewById<TextView>(R.id.reqMinuscula)
        val reqNumero    = findViewById<TextView>(R.id.reqNumero)
        val reqEspecial  = findViewById<TextView>(R.id.reqEspecial)

        val corOk    = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corErro  = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corMedio = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val corBom   = ContextCompat.getColor(this, android.R.color.holo_blue_light)

        editNova.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Remove o erro visual da nova senha enquanto o usuário corrige.
                layoutNova.error = null
                textoErro.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {
                val senha = s.toString()
                if (senha.isEmpty()) { layoutForca.visibility = View.GONE; return }
                layoutForca.visibility = View.VISIBLE

                val temMinimo    = senha.length >= 8
                val temMaiuscula = senha.any { it.isUpperCase() }
                val temMinuscula = senha.any { it.isLowerCase() }
                val temNumero    = senha.any { it.isDigit() }
                val temEspecial  = senha.any { !it.isLetterOrDigit() }

                fun atualiza(tv: TextView, ok: Boolean, msg: String) {
                    tv.text = if (ok) "✓  $msg" else "✗  $msg"
                    tv.setTextColor(if (ok) corOk else corErro)
                }
                atualiza(reqMinimo,    temMinimo,    "Mínimo 8 caracteres")
                atualiza(reqMaiuscula, temMaiuscula, "Pelo menos 1 letra maiúscula")
                atualiza(reqMinuscula, temMinuscula, "Pelo menos 1 letra minúscula")
                atualiza(reqNumero,    temNumero,    "Pelo menos 1 número")
                atualiza(reqEspecial,  temEspecial,  "Pelo menos 1 caractere especial (!@#\$...)")

                val pontos = listOf(temMinimo, temMaiuscula, temMinuscula, temNumero, temEspecial).count { it }
                listOf(barra1, barra2, barra3, barra4).forEach { it.setBackgroundColor(0xFFE0E0E0.toInt()) }

                when {
                    pontos <= 1 -> { barra1.setBackgroundColor(corErro); textoForca.text = "Senha muito fraca"; textoForca.setTextColor(corErro) }
                    pontos == 2 -> { barra1.setBackgroundColor(corErro); barra2.setBackgroundColor(corErro); textoForca.text = "Senha fraca"; textoForca.setTextColor(corErro) }
                    pontos == 3 -> { barra1.setBackgroundColor(corMedio); barra2.setBackgroundColor(corMedio); barra3.setBackgroundColor(corMedio); textoForca.text = "Senha razoável"; textoForca.setTextColor(corMedio) }
                    pontos == 4 -> { listOf(barra1,barra2,barra3,barra4).forEach { it.setBackgroundColor(corBom) }; textoForca.text = "Senha boa"; textoForca.setTextColor(corBom) }
                    else        -> { listOf(barra1,barra2,barra3,barra4).forEach { it.setBackgroundColor(corOk) }; textoForca.text = "Senha forte! ✓"; textoForca.setTextColor(corOk) }
                }
            }
        })

        botaoSalvar.setOnClickListener {
            val nova      = editNova.text.toString()
            val confirmar = editConfirmar.text.toString()
            textoErro.visibility  = View.GONE
            layoutNova.error      = null
            layoutConfirmar.error = null

            if (nova.isEmpty())      { layoutNova.error = "Informe a nova senha"; return@setOnClickListener }
            if (confirmar.isEmpty()) { layoutConfirmar.error = "Confirme a nova senha"; return@setOnClickListener }

            val temMinimo    = nova.length >= 8
            val temMaiuscula = nova.any { it.isUpperCase() }
            val temMinuscula = nova.any { it.isLowerCase() }
            val temNumero    = nova.any { it.isDigit() }
            val temEspecial  = nova.any { !it.isLetterOrDigit() }

            if (!temMinimo || !temMaiuscula || !temMinuscula || !temNumero || !temEspecial) {
                layoutNova.error = "A senha não atende todos os requisitos"
                return@setOnClickListener
            }
            if (nova != confirmar) {
                layoutConfirmar.error = "As senhas não coincidem"
                return@setOnClickListener
            }

            botaoSalvar.isEnabled = false
            botaoSalvar.text = "Salvando..."

            // ATUALIZADO: Enviando apenas o e-mail e a nova senha para o Back-end
            RetrofitClient.api.resetPassword(ResetPasswordRequest(email, nova))
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        botaoSalvar.isEnabled = true
                        botaoSalvar.text = "Salvar Nova Senha"

                        if (response.isSuccessful) {
                            val respostaServidor = response.body() ?: ""

                            // ATUALIZADO: Lendo se o servidor confirmou o SUCESSO
                            if (respostaServidor.contains("SUCESSO", ignoreCase = true)) {
                                // Após redefinir a senha com sucesso, volta direto para o login.
                                // As flags limpam o histórico para impedir voltar para as telas de recuperação.
                                val intent = Intent(this@NovaSenhaActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                textoErro.text = "⚠️ $respostaServidor"
                                textoErro.visibility = View.VISIBLE
                            }
                        } else {
                            textoErro.text = "⚠️ Erro no servidor: ${response.code()}"
                            textoErro.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        botaoSalvar.isEnabled = true
                        botaoSalvar.text = "Salvar Nova Senha"
                        textoErro.text = "⚠️ Falha na conexão com o servidor."
                        textoErro.visibility = View.VISIBLE
                    }
                })
        }
    }
}