package com.example.agend.diretor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SalaResponse
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.adapter.MinhasSalasAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MinhasSalasActivity : AppCompatActivity() {

    private lateinit var listaMinhasSalas: ListView
    private lateinit var textoErro: TextView
    private lateinit var botaoAtualizar: Button
    private lateinit var textoVoltar: TextView

    // Lista com as salas retornadas pelo back-end.
    private val salas = mutableListOf<SalaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela de salas cadastradas pelo diretor.
        setContentView(R.layout.activity_minhas_salas)

        listaMinhasSalas = findViewById(R.id.listaMinhasSalas)
        textoErro = findViewById(R.id.textoErroMinhasSalas)
        botaoAtualizar = findViewById(R.id.botaoAtualizarMinhasSalas)
        textoVoltar = findViewById(R.id.textoVoltarMinhasSalas)

        // Atualiza manualmente a lista de salas.
        botaoAtualizar.setOnClickListener {
            carregarMinhasSalas()
        }

        // Volta para a tela anterior.
        textoVoltar.setOnClickListener {
            finish()
        }

        carregarMinhasSalas()
    }

    private fun carregarMinhasSalas() {
        mostrarErro(null)

        botaoAtualizar.isEnabled = false
        botaoAtualizar.text = "Carregando..."

        RetrofitClient.api.listarMinhasSalas()
            .enqueue(object : Callback<List<SalaResponse>> {

                override fun onResponse(
                    call: Call<List<SalaResponse>>,
                    response: Response<List<SalaResponse>>
                ) {
                    botaoAtualizar.isEnabled = true
                    botaoAtualizar.text = "Atualizar"

                    if (response.isSuccessful) {
                        val resposta = response.body() ?: emptyList()

                        salas.clear()
                        salas.addAll(resposta)

                        if (salas.isEmpty()) {
                            // Estado vazio: mostra mensagem simples quando não há salas.
                            listaMinhasSalas.adapter = ArrayAdapter(
                                this@MinhasSalasActivity,
                                android.R.layout.simple_list_item_1,
                                listOf("Você ainda não possui salas cadastradas.")
                            )
                        } else {
                            // Mostra as salas em cards.
                            listaMinhasSalas.adapter = MinhasSalasAdapter(
                                context = this@MinhasSalasActivity,
                                salas = salas
                            )
                        }
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<List<SalaResponse>>, t: Throwable) {
                    botaoAtualizar.isEnabled = true
                    botaoAtualizar.text = "Atualizar"

                    mostrarErro("Falha na conexão ao carregar salas.")
                }
            })
    }

    private fun mostrarErro(mensagem: String?) {
        if (mensagem.isNullOrBlank()) {
            textoErro.text = ""
            textoErro.visibility = View.GONE
        } else {
            textoErro.text = mensagem
            textoErro.visibility = View.VISIBLE
        }
    }

    private fun tratarErroSessaoOuServidor(codigo: Int, erro: String?) {
        if (codigo == 401 || codigo == 403) {
            Toast.makeText(
                this,
                "Sessão expirada. Faça login novamente.",
                Toast.LENGTH_LONG
            ).show()

            // Limpa o token em memória.
            RetrofitClient.token = null

            // Limpa a sessão salva no celular.
            SessionManager(this).limparSessao()

            // Volta para o login e limpa a pilha de telas.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            mostrarErro(erro ?: "Erro no servidor: $codigo")
        }
    }
}