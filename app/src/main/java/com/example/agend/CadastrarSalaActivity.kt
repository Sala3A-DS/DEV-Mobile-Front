package com.example.agend

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SalaRequest
import com.example.agend.auth.SalaResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastrarSalaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega o layout da tela de cadastro de salas.
        setContentView(R.layout.activity_cadastrar_sala)

        val layoutNome = findViewById<TextInputLayout>(R.id.layoutNomeSala)
        val layoutBloco = findViewById<TextInputLayout>(R.id.layoutBlocoSala)
        val layoutCapacidade = findViewById<TextInputLayout>(R.id.layoutCapacidadeSala)
        val layoutRecursos = findViewById<TextInputLayout>(R.id.layoutRecursosSala)

        val editNome = findViewById<TextInputEditText>(R.id.editNomeSala)
        val editBloco = findViewById<TextInputEditText>(R.id.editBlocoSala)
        val editCapacidade = findViewById<TextInputEditText>(R.id.editCapacidadeSala)
        val editRecursos = findViewById<TextInputEditText>(R.id.editRecursosSala)

        val botaoSalvar = findViewById<Button>(R.id.botaoSalvarSala)
        val textoErro = findViewById<TextView>(R.id.textoErroSala)
        val textoVoltar = findViewById<TextView>(R.id.textoVoltarSala)

        botaoSalvar.setOnClickListener {
            textoErro.visibility = View.GONE

            layoutNome.error = null
            layoutBloco.error = null
            layoutCapacidade.error = null
            layoutRecursos.error = null

            val nome = editNome.text.toString().trim()
            val bloco = editBloco.text.toString().trim()
            val capacidadeTexto = editCapacidade.text.toString().trim()
            val recursosTexto = editRecursos.text.toString().trim()

            if (nome.isEmpty()) {
                layoutNome.error = "Informe o nome da sala"
                return@setOnClickListener
            }

            if (bloco.isEmpty()) {
                layoutBloco.error = "Informe o bloco ou localização"
                return@setOnClickListener
            }

            if (capacidadeTexto.isEmpty()) {
                layoutCapacidade.error = "Informe a capacidade"
                return@setOnClickListener
            }

            val capacidade = capacidadeTexto.toIntOrNull()

            if (capacidade == null || capacidade <= 0) {
                layoutCapacidade.error = "Informe uma capacidade válida"
                return@setOnClickListener
            }

            val recursos = if (recursosTexto.isEmpty()) {
                emptyList()
            } else {
                recursosTexto.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }

            val request = SalaRequest(
                nome = nome,
                bloco = bloco,
                capacidade = capacidade,
                recursos = recursos
            )

            botaoSalvar.isEnabled = false
            botaoSalvar.text = "Salvando..."

            RetrofitClient.api.cadastrarSala(request).enqueue(object : Callback<SalaResponse> {
                override fun onResponse(
                    call: Call<SalaResponse>,
                    response: Response<SalaResponse>
                ) {
                    botaoSalvar.isEnabled = true
                    botaoSalvar.text = "Salvar sala"

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@CadastrarSalaActivity,
                            "Sala cadastrada com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                    } else {
                        val erro = response.errorBody()?.string()

                        textoErro.text = erro ?: "Erro ao cadastrar sala: ${response.code()}"
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<SalaResponse>, t: Throwable) {
                    botaoSalvar.isEnabled = true
                    botaoSalvar.text = "Salvar sala"

                    textoErro.text = "Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        textoVoltar.setOnClickListener {
            finish()
        }
    }
}