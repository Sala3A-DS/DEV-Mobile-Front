package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.agend.auth.RegisterRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define o layout da tela de cadastro.
        setContentView(R.layout.activity_cadastro)

        // Campos principais do formulário.
        val layoutNome = findViewById<TextInputLayout>(R.id.layoutNome)
        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmailCadastro)
        val layoutSenha = findViewById<TextInputLayout>(R.id.layoutSenhaCadastro)
        val layoutConfirmarSenha = findViewById<TextInputLayout>(R.id.layoutConfirmarSenhaCadastro)

        // NOVO: campo de cargo personalizado.
        // Esse campo só aparece quando o usuário seleciona "Outros".
        val layoutCargoPersonalizado = findViewById<TextInputLayout>(R.id.layoutCargoPersonalizado)

        val editNome = findViewById<TextInputEditText>(R.id.editNome)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailCadastro)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenhaCadastro)
        val editConfirmarSenha = findViewById<TextInputEditText>(R.id.editConfirmarSenhaCadastro)

        // NOVO: input onde o usuário digita o cargo caso selecione "Outros".
        val editCargoPersonalizado = findViewById<TextInputEditText>(R.id.editCargoPersonalizado)

        val botaoCadastrar = findViewById<Button>(R.id.botaoCadastrar)
        val textoVoltarLogin = findViewById<TextView>(R.id.textoVoltarLogin)
        val radioGrupo = findViewById<RadioGroup>(R.id.radioGrupoPerfil)

        // Views do indicador de força da senha.
        val layoutForca = findViewById<LinearLayout>(R.id.layoutForcaSenha)
        val barra1 = findViewById<View>(R.id.barra1)
        val barra2 = findViewById<View>(R.id.barra2)
        val barra3 = findViewById<View>(R.id.barra3)
        val barra4 = findViewById<View>(R.id.barra4)
        val textoForca = findViewById<TextView>(R.id.textoForcaSenha)

        // Requisitos visuais da senha.
        val reqMaiuscula = findViewById<TextView>(R.id.reqMaiuscula)
        val reqMinuscula = findViewById<TextView>(R.id.reqMinuscula)
        val reqMinimo = findViewById<TextView>(R.id.reqMinimo)
        val reqNumero = findViewById<TextView>(R.id.reqNumero)
        val reqEspecial = findViewById<TextView>(R.id.reqEspecial)

        // Garante que o campo de cargo personalizado comece invisível.
        layoutCargoPersonalizado.visibility = View.GONE

        // Monitora a seleção do perfil.
        // Quando "Outros" for selecionado, o campo Cargo aparece.
        // Quando qualquer outro perfil for selecionado, ele desaparece e limpa o texto.
        radioGrupo.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioOutros) {
                layoutCargoPersonalizado.visibility = View.VISIBLE
            } else {
                layoutCargoPersonalizado.visibility = View.GONE
                layoutCargoPersonalizado.error = null
                editCargoPersonalizado.setText("")
            }
        }

        // Cores usadas na validação visual.
        val corOk = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corErro = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corFraco = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corMedio = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val corBom = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val corForte = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corInativo = 0xFFE0E0E0.toInt()

        // Monitora a senha em tempo real para atualizar os requisitos e a barra de força.
        editSenha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Não é necessário fazer nada antes da alteração.
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Não é necessário fazer nada durante a alteração.
            }

            override fun afterTextChanged(s: Editable?) {
                val senha = s.toString()

                // Se a senha estiver vazia, oculta o indicador de força.
                if (senha.isEmpty()) {
                    layoutForca.visibility = View.GONE
                    return
                }

                layoutForca.visibility = View.VISIBLE

                // Regras de senha forte.
                val temMaiuscula = senha.any { it.isUpperCase() }
                val temMinuscula = senha.any { it.isLowerCase() }
                val temMinimo = senha.length >= 8
                val temNumero = senha.any { it.isDigit() }
                val temEspecial = senha.any { !it.isLetterOrDigit() }

                // Atualiza os textos dos requisitos.
                reqMaiuscula.text =
                    if (temMaiuscula) "✓  Pelo menos 1 letra maiúscula"
                    else "✗  Pelo menos 1 letra maiúscula"

                reqMinuscula.text =
                    if (temMinuscula) "✓  Pelo menos 1 letra minúscula"
                    else "✗  Pelo menos 1 letra minúscula"

                reqMinimo.text =
                    if (temMinimo) "✓  Mínimo 8 caracteres"
                    else "✗  Mínimo 8 caracteres"

                reqNumero.text =
                    if (temNumero) "✓  Pelo menos 1 número"
                    else "✗  Pelo menos 1 número"

                reqEspecial.text =
                    if (temEspecial) "✓  Pelo menos 1 caractere especial (!@#\$...)"
                    else "✗  Pelo menos 1 caractere especial (!@#\$...)"

                // Atualiza as cores dos requisitos.
                reqMaiuscula.setTextColor(if (temMaiuscula) corOk else corErro)
                reqMinuscula.setTextColor(if (temMinuscula) corOk else corErro)
                reqMinimo.setTextColor(if (temMinimo) corOk else corErro)
                reqNumero.setTextColor(if (temNumero) corOk else corErro)
                reqEspecial.setTextColor(if (temEspecial) corOk else corErro)

                // Conta quantos requisitos foram atendidos.
                val pontos = listOf(
                    temMaiuscula,
                    temMinuscula,
                    temMinimo,
                    temNumero,
                    temEspecial
                ).count { it }

                val barras = listOf(barra1, barra2, barra3, barra4)

                // Reseta as barras antes de pintar novamente.
                barras.forEach { it.setBackgroundColor(corInativo) }

                // Atualiza o nível visual da senha.
                when (pontos) {
                    1 -> {
                        barra1.setBackgroundColor(corFraco)
                        textoForca.text = "Senha muito fraca"
                        textoForca.setTextColor(corFraco)
                    }

                    2 -> {
                        barra1.setBackgroundColor(corFraco)
                        barra2.setBackgroundColor(corFraco)
                        textoForca.text = "Senha fraca"
                        textoForca.setTextColor(corFraco)
                    }

                    3 -> {
                        barra1.setBackgroundColor(corMedio)
                        barra2.setBackgroundColor(corMedio)
                        barra3.setBackgroundColor(corMedio)
                        textoForca.text = "Senha razoável"
                        textoForca.setTextColor(corMedio)
                    }

                    4 -> {
                        barra1.setBackgroundColor(corBom)
                        barra2.setBackgroundColor(corBom)
                        barra3.setBackgroundColor(corBom)
                        barra4.setBackgroundColor(corBom)
                        textoForca.text = "Senha boa"
                        textoForca.setTextColor(corBom)
                    }

                    5 -> {
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
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim().lowercase()
            val senha = editSenha.text.toString().trim()
            val confirmarSenha = editConfirmarSenha.text.toString().trim()
            val cargoPersonalizado = editCargoPersonalizado.text.toString().trim()

            val perfilId = radioGrupo.checkedRadioButtonId

            // Limpa erros anteriores.
            layoutNome.error = null
            layoutEmail.error = null
            layoutSenha.error = null
            layoutConfirmarSenha.error = null
            layoutCargoPersonalizado.error = null

            // Validação do nome.
            if (nome.isEmpty()) {
                layoutNome.error = "Informe seu nome"
                return@setOnClickListener
            }

            // Validação do e-mail.
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            // Validação da senha.
            if (senha.isEmpty()) {
                layoutSenha.error = "Informe a senha"
                return@setOnClickListener
            }

            // Validação do campo confirmar senha.
            if (confirmarSenha.isEmpty()) {
                layoutConfirmarSenha.error = "Confirme sua senha"
                return@setOnClickListener
            }

            // Verifica se senha e confirmação são iguais.
            if (senha != confirmarSenha) {
                layoutConfirmarSenha.error = "As senhas não coincidem"
                return@setOnClickListener
            }

            // Regras obrigatórias da senha forte.
            val temMaiuscula = senha.any { it.isUpperCase() }
            val temMinuscula = senha.any { it.isLowerCase() }
            val temMinimo = senha.length >= 8
            val temNumero = senha.any { it.isDigit() }
            val temEspecial = senha.any { !it.isLetterOrDigit() }

            // A senha só passa se tiver todos os requisitos.
            if (!temMaiuscula || !temMinuscula || !temMinimo || !temNumero || !temEspecial) {
                layoutSenha.error = "A senha não atende os requisitos mínimos"
                Toast.makeText(
                    this,
                    "A senha deve ter maiúscula, minúscula, número, caractere especial e mínimo de 8 caracteres.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Validação do perfil.
            if (perfilId == -1) {
                Toast.makeText(this, "⚠️ Selecione seu perfil!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // NOVO: se o usuário escolher "Outros", o campo Cargo se torna obrigatório.
            if (perfilId == R.id.radioOutros && cargoPersonalizado.isEmpty()) {
                layoutCargoPersonalizado.error = "Informe seu cargo"
                return@setOnClickListener
            }

            // Define o cargo enviado ao back-end.
            // Professor, coordenador e diretor usam valores padronizados.
            // Outros usa o cargo digitado pela pessoa, em letras maiúsculas.
            val cargo = when (perfilId) {
                R.id.radioProfessor -> "PROFESSOR"
                R.id.radioCoordenador -> "COORDENADOR"
                R.id.radioOutros -> cargoPersonalizado.uppercase()
                else -> ""
            }

            // Bloqueia o botão para evitar vários cadastros ao mesmo tempo.
            botaoCadastrar.isEnabled = false
            botaoCadastrar.text = "Cadastrando..."

            // Envia o cargo final para o back-end.
            // O back-end já recebe apenas uma string "cargo".
            val pedido = RegisterRequest(nome, email, senha, cargo)

            RetrofitClient.api.register(pedido).enqueue(object : Callback<String> {

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"

                    if (response.isSuccessful) {
                        val respostaServidor = response.body() ?: ""

                        if (respostaServidor.contains("SUCESSO", ignoreCase = true)) {
                            Toast.makeText(
                                this@CadastroActivity,
                                "✅ Cadastrado com sucesso!",
                                Toast.LENGTH_LONG
                            ).show()

                            // Após cadastrar, volta para a tela de login.
                            startActivity(
                                Intent(
                                    this@CadastroActivity,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        } else if (respostaServidor.contains("ERRO", ignoreCase = true)) {
                            Toast.makeText(
                                this@CadastroActivity,
                                "⚠️ $respostaServidor",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@CadastroActivity,
                                "⚠️ Resposta inesperada do servidor: $respostaServidor",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val erroServidor = response.errorBody()?.string()

                        Toast.makeText(
                            this@CadastroActivity,
                            erroServidor ?: "⚠️ Erro no servidor: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"

                    // Log importante para descobrir o erro real no Logcat.
                    Log.e("CADASTRO", "Falha ao cadastrar usuário: ${t.message}", t)

                    Toast.makeText(
                        this@CadastroActivity,
                        "⚠️ Falha na conexão com o servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        // Botão/texto para voltar para a tela de login.
        textoVoltarLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}