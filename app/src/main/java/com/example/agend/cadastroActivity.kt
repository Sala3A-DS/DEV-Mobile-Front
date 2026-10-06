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
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout

class CadastroActivity : AppCompatActivity() {

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

    // Remove o erro visual do campo assim que o usuário começa a corrigi-lo.
    private fun configurarLimpezaErro(
        editText: TextInputEditText,
        layout: TextInputLayout
    ) {
        editText.addTextChangedListener(object : TextWatcher {

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
                layout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de cadastro.
        setContentView(R.layout.activity_cadastro)

        // Retorna para a tela anterior.
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        // Campos de entrada e seus respectivos containers de validação.
        val layoutNome = findViewById<TextInputLayout>(R.id.layoutNome)
        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmailCadastro)
        val layoutSenha = findViewById<TextInputLayout>(R.id.layoutSenhaCadastro)
        val layoutConfirmarSenha =
            findViewById<TextInputLayout>(R.id.layoutConfirmarSenhaCadastro)

        // Campo adicional exibido somente para o perfil "Outros".
        val layoutCargoPersonalizado =
            findViewById<TextInputLayout>(R.id.layoutCargoPersonalizado)

        val editNome = findViewById<TextInputEditText>(R.id.editNome)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailCadastro)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenhaCadastro)
        val editConfirmarSenha =
            findViewById<TextInputEditText>(R.id.editConfirmarSenhaCadastro)

        val editCargoPersonalizado =
            findViewById<TextInputEditText>(R.id.editCargoPersonalizado)

        // Configura a remoção automática dos erros durante a edição dos campos.
        configurarLimpezaErro(editNome, layoutNome)
        configurarLimpezaErro(editEmail, layoutEmail)
        configurarLimpezaErro(editConfirmarSenha, layoutConfirmarSenha)
        configurarLimpezaErro(editCargoPersonalizado, layoutCargoPersonalizado)

        val botaoCadastrar = findViewById<Button>(R.id.botaoCadastrar)
        val radioGrupo = findViewById<RadioGroup>(R.id.radioGrupoPerfil)

        // Componentes utilizados para indicar a força da senha.
        val layoutForca = findViewById<LinearLayout>(R.id.layoutForcaSenha)
        val barra1 = findViewById<View>(R.id.barra1)
        val barra2 = findViewById<View>(R.id.barra2)
        val barra3 = findViewById<View>(R.id.barra3)
        val barra4 = findViewById<View>(R.id.barra4)
        val textoForca = findViewById<TextView>(R.id.textoForcaSenha)

        // Indicadores dos requisitos mínimos da senha.
        val reqMaiuscula = findViewById<TextView>(R.id.reqMaiuscula)
        val reqMinuscula = findViewById<TextView>(R.id.reqMinuscula)
        val reqMinimo = findViewById<TextView>(R.id.reqMinimo)
        val reqNumero = findViewById<TextView>(R.id.reqNumero)
        val reqEspecial = findViewById<TextView>(R.id.reqEspecial)

        // O campo de cargo personalizado permanece oculto inicialmente.
        layoutCargoPersonalizado.visibility = View.GONE

        // Exibe o campo de cargo quando o perfil "Outros" é selecionado.
        radioGrupo.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioOutros) {
                layoutCargoPersonalizado.visibility = View.VISIBLE
            } else {
                layoutCargoPersonalizado.visibility = View.GONE
                layoutCargoPersonalizado.error = null
                editCargoPersonalizado.setText("")
            }
        }

        // Cores utilizadas nos estados dos requisitos e da força da senha.
        val corOk = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corErro = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corFraco = ContextCompat.getColor(this, android.R.color.holo_red_light)
        val corMedio = ContextCompat.getColor(this, android.R.color.holo_orange_light)
        val corBom = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val corForte = ContextCompat.getColor(this, android.R.color.holo_green_light)
        val corInativo = 0xFFE0E0E0.toInt()

        // Atualiza os requisitos e a força da senha em tempo real.
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
            }

            override fun afterTextChanged(s: Editable?) {
                val senha = s.toString()

                // Oculta o indicador quando não há senha digitada.
                if (senha.isEmpty()) {
                    layoutForca.visibility = View.GONE
                    return
                }

                layoutForca.visibility = View.VISIBLE

                // Avalia os requisitos mínimos de segurança da senha.
                val temMaiuscula = senha.any { it.isUpperCase() }
                val temMinuscula = senha.any { it.isLowerCase() }
                val temMinimo = senha.length >= 8
                val temNumero = senha.any { it.isDigit() }
                val temEspecial = senha.any { !it.isLetterOrDigit() }

                // Atualiza a indicação textual de cada requisito.
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

                // Atualiza as cores conforme o atendimento dos requisitos.
                reqMaiuscula.setTextColor(if (temMaiuscula) corOk else corErro)
                reqMinuscula.setTextColor(if (temMinuscula) corOk else corErro)
                reqMinimo.setTextColor(if (temMinimo) corOk else corErro)
                reqNumero.setTextColor(if (temNumero) corOk else corErro)
                reqEspecial.setTextColor(if (temEspecial) corOk else corErro)

                // Calcula o nível de força com base nos requisitos atendidos.
                val pontos = listOf(
                    temMaiuscula,
                    temMinuscula,
                    temMinimo,
                    temNumero,
                    temEspecial
                ).count { it }

                val barras = listOf(
                    barra1,
                    barra2,
                    barra3,
                    barra4
                )

                // Reseta o indicador antes de aplicar o novo nível.
                barras.forEach {
                    it.setBackgroundColor(corInativo)
                }

                // Define a classificação visual da senha.
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
                        barras.forEach {
                            it.setBackgroundColor(corForte)
                        }
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

            // Remove os erros exibidos antes de uma nova tentativa.
            layoutNome.error = null
            layoutEmail.error = null
            layoutSenha.error = null
            layoutConfirmarSenha.error = null
            layoutCargoPersonalizado.error = null

            // Valida o nome.
            if (nome.isEmpty()) {
                layoutNome.error = "Informe seu nome"
                return@setOnClickListener
            }

            // Valida o preenchimento e o formato do e-mail.
            if (email.isEmpty()) {
                layoutEmail.error = "Informe o e-mail"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                layoutEmail.error = "Informe um e-mail válido (ex: nome@dominio.com)"
                return@setOnClickListener
            }

            // O cargo personalizado é obrigatório quando "Outros" está selecionado.
            if (perfilId == R.id.radioOutros && cargoPersonalizado.isEmpty()) {
                layoutCargoPersonalizado.error = "Informe seu cargo"
                return@setOnClickListener
            }

            // Converte o perfil selecionado para o valor esperado pelo backend.
            val cargo = when (perfilId) {
                R.id.radioProfessor -> "PROFESSOR"
                R.id.radioCoordenador -> "COORDENADOR"
                R.id.radioOutros -> cargoPersonalizado.uppercase()
                else -> ""
            }

            // Valida o preenchimento da senha.
            if (senha.isEmpty()) {
                layoutSenha.error = "Informe a senha"
                return@setOnClickListener
            }

            if (confirmarSenha.isEmpty()) {
                layoutConfirmarSenha.error = "Confirme sua senha"
                return@setOnClickListener
            }

            // Garante que a confirmação corresponda à senha informada.
            if (senha != confirmarSenha) {
                layoutConfirmarSenha.error = "As senhas não coincidem"
                return@setOnClickListener
            }

            // Avalia os requisitos obrigatórios da senha.
            val temMaiuscula = senha.any { it.isUpperCase() }
            val temMinuscula = senha.any { it.isLowerCase() }
            val temMinimo = senha.length >= 8
            val temNumero = senha.any { it.isDigit() }
            val temEspecial = senha.any { !it.isLetterOrDigit() }

            // Impede o cadastro enquanto algum requisito de segurança não for atendido.
            if (!temMaiuscula || !temMinuscula || !temMinimo || !temNumero || !temEspecial) {
                layoutSenha.error = "A senha não atende os requisitos mínimos"

                Toast.makeText(
                    this,
                    "A senha deve ter maiúscula, minúscula, número, caractere especial e mínimo de 8 caracteres.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            // Garante que um perfil tenha sido selecionado.
            if (perfilId == -1) {
                Toast.makeText(
                    this,
                    "⚠️ Selecione seu perfil!",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Evita múltiplas requisições durante o cadastro.
            botaoCadastrar.isEnabled = false
            botaoCadastrar.text = "Cadastrando..."

            // Monta o payload enviado ao backend.
            val pedido = RegisterRequest(nome, email, senha, cargo)

            RetrofitClient.api.register(pedido).enqueue(object : Callback<String> {

                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
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

                            // Após o cadastro, retorna o usuário para a tela de login.
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
                            // Trata respostas que não seguem o formato esperado.
                            Toast.makeText(
                                this@CadastroActivity,
                                "⚠️ Resposta inesperada do servidor: $respostaServidor",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        // Exibe a mensagem retornada pelo backend quando disponível.
                        val erroServidor = response.errorBody()?.string()

                        Toast.makeText(
                            this@CadastroActivity,
                            erroServidor ?: "⚠️ Erro no servidor: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<String>,
                    t: Throwable
                ) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"

                    // Registra o erro completo para análise no Logcat.
                    Log.e(
                        "CADASTRO",
                        "Falha ao cadastrar usuário: ${t.message}",
                        t
                    )

                    Toast.makeText(
                        this@CadastroActivity,
                        "⚠️ Falha na conexão com o servidor.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }
}