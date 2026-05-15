package com.example.agend.diretor

import android.content.Intent
import com.example.agend.diretor.adapter.AgendamentosGeraisAdapter
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
import com.example.agend.auth.ReservaResponse
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AgendamentosGeraisActivity : AppCompatActivity() {

    private lateinit var listaAgendamentos: ListView
    private lateinit var textoErro: TextView
    private lateinit var botaoAtualizar: Button
    private lateinit var textoVoltar: TextView

    // Lista com todas as reservas retornadas pelo back-end.
    private val reservas = mutableListOf<ReservaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela de agendamentos gerais do diretor.
        setContentView(R.layout.activity_agendamentos_gerais)

        listaAgendamentos = findViewById(R.id.listaAgendamentosGerais)
        textoErro = findViewById(R.id.textoErroAgendamentosGerais)
        botaoAtualizar = findViewById(R.id.botaoAtualizarAgendamentosGerais)
        textoVoltar = findViewById(R.id.textoVoltarAgendamentosGerais)

        // Atualiza manualmente a lista.
        botaoAtualizar.setOnClickListener {
            carregarAgendamentosGerais()
        }

        // Volta para o painel do diretor.
        textoVoltar.setOnClickListener {
            finish()
        }

        carregarAgendamentosGerais()
    }

    private fun carregarAgendamentosGerais() {
        mostrarErro(null)

        botaoAtualizar.isEnabled = false
        botaoAtualizar.text = "Carregando..."

        RetrofitClient.api.listarReservasGerais()
            .enqueue(object : Callback<List<ReservaResponse>> {

                override fun onResponse(
                    call: Call<List<ReservaResponse>>,
                    response: Response<List<ReservaResponse>>
                ) {
                    botaoAtualizar.isEnabled = true
                    botaoAtualizar.text = "Atualizar"

                    if (response.isSuccessful) {
                        val resposta = response.body() ?: emptyList()

                        reservas.clear()
                        reservas.addAll(resposta)

                        if (reservas.isEmpty()) {
                            // Estado vazio quando ainda não existem agendamentos.
                            listaAgendamentos.adapter = ArrayAdapter(
                                this@AgendamentosGeraisActivity,
                                android.R.layout.simple_list_item_1,
                                listOf("Nenhum agendamento encontrado.")
                            )
                        } else {
                            // Mostra os agendamentos em cards.
                            listaAgendamentos.adapter = AgendamentosGeraisAdapter(
                                context = this@AgendamentosGeraisActivity,
                                reservas = reservas
                            )
                        }
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<List<ReservaResponse>>, t: Throwable) {
                    botaoAtualizar.isEnabled = true
                    botaoAtualizar.text = "Atualizar"

                    mostrarErro("Falha na conexão ao carregar agendamentos.")
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