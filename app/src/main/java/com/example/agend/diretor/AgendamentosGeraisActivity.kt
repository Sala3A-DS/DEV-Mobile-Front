package com.example.agend.diretor

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.ReservaResponse
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.adapter.AgendamentosGeraisAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AgendamentosGeraisActivity : AppCompatActivity() {

    private lateinit var listaAgendamentos: ListView
    private lateinit var textoErro: TextView
    private lateinit var botaoAtualizar: Button

    private lateinit var layoutData: TextInputLayout
    private lateinit var editData: TextInputEditText

    private var dataSelecionada: String = ""

    // Armazena todos os agendamentos retornados pelo backend.
    private val reservas = mutableListOf<ReservaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de agendamentos gerais do diretor.
        setContentView(R.layout.activity_agendamentos_gerais)

        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        // Configura o retorno para a tela anterior.
        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        listaAgendamentos = findViewById(R.id.listaAgendamentosGerais)
        textoErro = findViewById(R.id.textoErroAgendamentosGerais)
        botaoAtualizar = findViewById(R.id.botaoAtualizarAgendamentosGerais)
        layoutData = findViewById(R.id.layoutDataAgendamentosGerais)
        editData = findViewById(R.id.editDataAgendamentosGerais)

        editData.setOnClickListener {
            abrirDatePicker()
        }

        // Atualiza os agendamentos da data selecionada.
        botaoAtualizar.setOnClickListener {
            if (dataSelecionada.isBlank()) {
                layoutData.error = "Selecione uma data"
                return@setOnClickListener
            }

            carregarAgendamentosGerais()
        }

        // Define automaticamente a data atual.
        definirDataHoje()

        // Carrega os agendamentos do dia atual.
        carregarAgendamentosGerais()
    }

    private fun definirDataHoje() {
        val calendario = Calendar.getInstance()

        val ano = calendario.get(Calendar.YEAR)
        val mes = (calendario.get(Calendar.MONTH) + 1)
            .toString()
            .padStart(2, '0')

        val dia = calendario.get(Calendar.DAY_OF_MONTH)
            .toString()
            .padStart(2, '0')

        dataSelecionada = "$ano-$mes-$dia"
        editData.setText(dataSelecionada)
    }

    private fun abrirDatePicker() {
        val calendario = Calendar.getInstance()

        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                val mesFormatado =
                    (mesSelecionado + 1).toString().padStart(2, '0')

                val diaFormatado =
                    diaSelecionado.toString().padStart(2, '0')

                dataSelecionada =
                    "$anoSelecionado-$mesFormatado-$diaFormatado"

                editData.setText(dataSelecionada)

                // Remove o erro após a seleção da data.
                layoutData.error = null
                mostrarErro(null)

                // Atualiza os agendamentos da nova data.
                carregarAgendamentosGerais()
            },
            ano,
            mes,
            dia
        )

        dialog.show()
    }

    private fun carregarAgendamentosGerais() {
        mostrarErro(null)

        botaoAtualizar.isEnabled = false
        botaoAtualizar.text = "Carregando..."

        // Consulta ao backend os agendamentos da data selecionada.
        RetrofitClient.api.listarReservasGerais(dataSelecionada)
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
                            // Exibe o estado vazio quando não existem agendamentos.
                            listaAgendamentos.adapter = ArrayAdapter(
                                this@AgendamentosGeraisActivity,
                                android.R.layout.simple_list_item_1,
                                listOf("Nenhum agendamento encontrado.")
                            )
                        } else {
                            // Exibe os agendamentos utilizando o adapter personalizado.
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

                override fun onFailure(
                    call: Call<List<ReservaResponse>>,
                    t: Throwable
                ) {
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

    private fun tratarErroSessaoOuServidor(
        codigo: Int,
        erro: String?
    ) {
        if (codigo == 401 || codigo == 403) {
            Toast.makeText(
                this,
                "Sessão expirada. Faça login novamente.",
                Toast.LENGTH_LONG
            ).show()

            // Remove o token da sessão em memória.
            RetrofitClient.token = null

            // Remove os dados da sessão persistida.
            SessionManager(this).limparSessao()

            // Retorna ao login e remove as telas protegidas da pilha.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        } else {
            mostrarErro(erro ?: "Erro no servidor: $codigo")
        }
    }
}