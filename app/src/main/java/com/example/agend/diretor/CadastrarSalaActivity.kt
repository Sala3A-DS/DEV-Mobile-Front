package com.example.agend.diretor

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.R
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SalaRequest
import com.example.agend.auth.SalaResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout

class CadastrarSalaActivity : AppCompatActivity() {

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
                // Nenhuma ação necessária antes da alteração.
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Remove o erro enquanto o usuário corrige o campo.
                layout.error = null
                textoErro.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Nenhuma ação necessária após a alteração.
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de cadastro de espaços.
        setContentView(R.layout.activity_cadastrar_sala)

        // Configura o botão de retorno para fechar a tela atual.
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        // Inicializa os campos e componentes do formulário.
        val layoutNome = findViewById<TextInputLayout>(R.id.layoutNomeSala)
        val layoutLocalizacao = findViewById<TextInputLayout>(R.id.layoutBlocoSala)
        val layoutNumeroSala = findViewById<TextInputLayout>(R.id.layoutCapacidadeSala)

        val editNome = findViewById<TextInputEditText>(R.id.editNomeSala)
        val editLocalizacao = findViewById<TextInputEditText>(R.id.editBlocoSala)
        val editNumeroSala = findViewById<TextInputEditText>(R.id.editCapacidadeSala)

        val botaoSalvar = findViewById<Button>(R.id.botaoSalvarSala)
        val textoErro = findViewById<TextView>(R.id.textoErroSala)

        // Remove automaticamente os erros durante a edição dos campos.
        configurarLimpezaErro(editNome, layoutNome, textoErro)
        configurarLimpezaErro(editLocalizacao, layoutLocalizacao, textoErro)
        configurarLimpezaErro(editNumeroSala, layoutNumeroSala, textoErro)

        botaoSalvar.setOnClickListener {
            textoErro.visibility = View.GONE

            // Limpa os erros antes de iniciar uma nova validação.
            layoutNome.error = null
            layoutLocalizacao.error = null
            layoutNumeroSala.error = null

            val nomeEspaco = editNome.text.toString().trim()
            val localizacao = editLocalizacao.text.toString().trim()
            val numeroSalaTexto = editNumeroSala.text.toString().trim()

            // Valida o preenchimento do nome do espaço.
            if (nomeEspaco.isEmpty()) {
                layoutNome.error = "Informe o nome do espaço"
                return@setOnClickListener
            }

            // Valida o preenchimento da localização.
            if (localizacao.isEmpty()) {
                layoutLocalizacao.error = "Informe a localização"
                return@setOnClickListener
            }

            // Valida o preenchimento do número da sala.
            if (numeroSalaTexto.isEmpty()) {
                layoutNumeroSala.error = "Informe o número da sala"
                return@setOnClickListener
            }

            val numeroSala = numeroSalaTexto.toIntOrNull()

            // Garante que o número informado seja válido e positivo.
            if (numeroSala == null || numeroSala <= 0) {
                layoutNumeroSala.error = "Informe um número de sala válido"
                return@setOnClickListener
            }

            val request = SalaRequest(
                nomeEspaco = nomeEspaco,
                localizacao = localizacao,
                numeroSala = numeroSala
            )

            // Evita múltiplos envios enquanto o cadastro está em andamento.
            botaoSalvar.isEnabled = false
            botaoSalvar.text = "Salvando..."

            RetrofitClient.api.cadastrarSala(request).enqueue(object : Callback<SalaResponse> {
                override fun onResponse(
                    call: Call<SalaResponse>,
                    response: Response<SalaResponse>
                ) {
                    botaoSalvar.isEnabled = true
                    botaoSalvar.text = "Salvar espaço"

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@CadastrarSalaActivity,
                            "Espaço cadastrado com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                    } else {
                        // Exibe a mensagem retornada pelo backend ou o código HTTP.
                        val erro = response.errorBody()?.string()

                        textoErro.text = erro ?: "Erro ao cadastrar espaço: ${response.code()}"
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<SalaResponse>, t: Throwable) {
                    botaoSalvar.isEnabled = true
                    botaoSalvar.text = "Salvar espaço"

                    // Informa ao usuário quando não foi possível acessar o servidor.
                    textoErro.text = "Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }
    }
}