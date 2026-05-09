package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.agend.auth.RegisterRequest
import com.example.agend.auth.RetrofitClient // Certifique-se de que este import está correto
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        val layoutNome        = findViewById<TextInputLayout>(R.id.layoutNome)
        val layoutEmail       = findViewById<TextInputLayout>(R.id.layoutEmailCadastro)
        val layoutSenha       = findViewById<TextInputLayout>(R.id.layoutSenhaCadastro)
        val editNome          = findViewById<TextInputEditText>(R.id.editNome)
        val editEmail         = findViewById<TextInputEditText>(R.id.editEmailCadastro)
        val editSenha         = findViewById<TextInputEditText>(R.id.editSenhaCadastro)
        val botaoCadastrar    = findViewById<Button>(R.id.botaoCadastrar)
        val textoVoltarLogin  = findViewById<TextView>(R.id.textoVoltarLogin)
        val radioGrupo        = findViewById<RadioGroup>(R.id.radioGrupoPerfil)

        // Views do indicador de força
        val layoutForca       = findViewById<LinearLayout>(R.id.layoutForcaSenha)
        val barra1            = findViewById<View>(R.id.barra1)
        val barra2            = findViewById<View>(R.id.barra2)
        val barra3            = findViewById<View>(R.id.barra3)
        val barra4            = findViewById<View>(R.id.barra4)
        val textoForca        = findViewById<TextView>(R.id.textoForcaSenha)
        val reqMinimo         = findViewById<TextView>(R.id.reqMinimo)
        val reqNumero         = findViewById<TextView>(R.id.reqNumero)
        val reqEspecial       = findViewById<TextView>(R.id.reqEspecial)

        val corOk      = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corErro    = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corFraco   = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corMedio   = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val corBom     = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val corForte   = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corInativo = 0xFFE0E0E0.toInt()

        // Monitora a senha em tempo real
        editSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val senha = s.toString()

                if (senha.isEmpty()) {
                    layoutForca.visibility = View.GONE
                    return
                }

                layoutForca.visibility = View.VISIBLE

                val temMinimo   = senha.length >= 8
                val temNumero   = senha.any { it.isDigit() }
                val temEspecial = senha.any { !it.isLetterOrDigit() }

                reqMinimo.text  = if (temMinimo)   "✓  Mínimo 8 caracteres"                   else "✗  Mínimo 8 caracteres"
                reqNumero.text  = if (temNumero)   "✓  Pelo menos 1 número"                    else "✗  Pelo menos 1 número"
                reqEspecial.text= if (temEspecial) "✓  Pelo menos 1 caractere especial (!@#\$...)" else "✗  Pelo menos 1 caractere especial (!@#\$...)"

                reqMinimo.setTextColor(if (temMinimo)   corOk else corErro)
                reqNumero.setTextColor(if (temNumero)   corOk else corErro)
                reqEspecial.setTextColor(if (temEspecial) corOk else corErro)

                val pontos = listOf(temMinimo, temNumero, temEspecial, senha.length >= 12).count { it }

                val barras = listOf(barra1, barra2, barra3, barra4)
                barras.forEach { it.setBackgroundColor(corInativo) }

                when (pontos) {
                    1 -> {
                        barra1.setBackgroundColor(corFraco)
                        textoForca.text = "Senha fraca"
                        textoForca.setTextColor(corFraco)
                    }
                    2 -> {
                        barra1.setBackgroundColor(corMedio)
                        barra2.setBackgroundColor(corMedio)
                        textoForca.text = "Senha razoável"
                        textoForca.setTextColor(corMedio)
                    }
                    3 -> {
                        barra1.setBackgroundColor(corBom)
                        barra2.setBackgroundColor(corBom)
                        barra3.setBackgroundColor(corBom)
                        textoForca.text = "Senha boa"
                        textoForca.setTextColor(corBom)
                    }
                    4 -> {
                        barras.forEach { it.setBackgroundColor(corForte) }
                        textoForca.text = "Senha forte! ✓"
                        textoForca.setTextColor(corForte)
                    }
                    else -> {
                        textoForca.text = "Senha muito fraca"
                        textoForca.setTextColor(corFraco)
                    }
                }
            }
        })

        botaoCadastrar.setOnClickListener {
            val nome    = editNome.text.toString().trim()
            val email   = editEmail.text.toString().trim()
            val senha   = editSenha.text.toString().trim()
            val perfilId = radioGrupo.checkedRadioButtonId

            layoutNome.error  = null
            layoutEmail.error = null
            layoutSenha.error = null

            if (nome.isEmpty()) {
                layoutNome.error = "Informe seu nome"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                layoutSenha.error = "Informe a senha"
                return@setOnClickListener
            }

            val temMinimo   = senha.length >= 8
            val temNumero   = senha.any { it.isDigit() }
            val temEspecial = senha.any { !it.isLetterOrDigit() }

            if (!temMinimo || !temNumero || !temEspecial) {
                layoutSenha.error = "A senha não atende os requisitos mínimos"
                return@setOnClickListener
            }

            if (perfilId == -1) {
                Toast.makeText(this, "⚠️ Selecione seu perfil!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cargo = when (perfilId) {
                R.id.radioProfessor -> "PROFESSOR"
                R.id.radioDiretor   -> "ADM"
                else                -> ""
            }

            botaoCadastrar.isEnabled = false
            botaoCadastrar.text = "Cadastrando..."

            val pedido = RegisterRequest(nome, email, senha, cargo)

            RetrofitClient.api.register(pedido).enqueue(object : Callback<String> {

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"

                    if (response.isSuccessful) {
                        val respostaServidor = response.body() ?: ""

                        // ATUALIZAÇÃO: Lendo o texto real que o servidor devolveu
                        if (respostaServidor.contains("SUCESSO")) {
                            Toast.makeText(
                                this@CadastroActivity,
                                "✅ Cadastrado com sucesso!",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(
                                Intent(
                                    this@CadastroActivity,
                                    MainActivity::class.java
                                )
                            ) // Volta pro Login
                            finish()
                        } else if (respostaServidor.contains("ERRO")) {
                            // Se o back-end disse que já existe
                            Toast.makeText(
                                this@CadastroActivity,
                                "⚠️ $respostaServidor",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@CadastroActivity,
                                "⚠️ Erro desconhecido.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@CadastroActivity,
                            "⚠️ Erro no servidor: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"
                    Toast.makeText(
                        this@CadastroActivity,
                        "⚠️ Falha na conexão com o servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        // Faltava esse botão de voltar que sumiu do finalzinho
        textoVoltarLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    } // Fecha o onCreate
} // Fecha a classe