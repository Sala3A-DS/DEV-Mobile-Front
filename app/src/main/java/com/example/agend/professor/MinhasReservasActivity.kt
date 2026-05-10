package com.example.agend.professor

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.ReservaResponse
import com.example.agend.auth.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.agend.R

class MinhasReservasActivity : AppCompatActivity() {

    private lateinit var listaReservas: ListView
    private lateinit var textoErro: TextView
    private lateinit var botaoAtualizar: Button
    private lateinit var textoVoltar: TextView

    private lateinit var adapter: ArrayAdapter<String>

    private val reservas = mutableListOf<ReservaResponse>()
    private val reservasFormatadas = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_minhas_reservas)

        listaReservas = findViewById(R.id.listaMinhasReservas)
        textoErro = findViewById(R.id.textoErroMinhasReservas)
        botaoAtualizar = findViewById(R.id.botaoAtualizarReservas)
        textoVoltar = findViewById(R.id.textoVoltarMinhasReservas)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            reservasFormatadas
        )

        listaReservas.adapter = adapter

        botaoAtualizar.setOnClickListener {
            carregarMinhasReservas()
        }

        textoVoltar.setOnClickListener {
            finish()
        }

        listaReservas.setOnItemClickListener { _, _, position, _ ->
            val reserva = reservas.getOrNull(position) ?: return@setOnItemClickListener
            confirmarCancelamento(reserva)
        }

        carregarMinhasReservas()
    }

    private fun carregarMinhasReservas() {
        mostrarErro(null)

        botaoAtualizar.isEnabled = false
        botaoAtualizar.text = "Carregando..."

        RetrofitClient.api.listarMinhasReservas()
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
                        reservasFormatadas.clear()

                        reservas.addAll(resposta)

                        if (reservas.isEmpty()) {
                            reservasFormatadas.add("Você ainda não possui reservas.")
                        } else {
                            reservasFormatadas.addAll(
                                reservas.map { reserva ->
                                    montarTextoReserva(reserva)
                                }
                            )
                        }

                        adapter.notifyDataSetChanged()
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

                    mostrarErro("Falha na conexão ao carregar reservas.")
                }
            })
    }

    private fun montarTextoReserva(reserva: ReservaResponse): String {
        val periodo = "${reserva.periodoAula}ª aula"
        val horario = "${reserva.horarioInicio} às ${reserva.horarioFim}"

        return """
            Sala: ${reserva.salaNome}
            Data: ${reserva.data}
            Horário: $periodo - $horario
            Status: ${reserva.status}
            
            Toque para cancelar
        """.trimIndent()
    }

    private fun confirmarCancelamento(reserva: ReservaResponse) {
        if (reserva.id.isNullOrBlank()) {
            mostrarErro("Reserva inválida.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Cancelar reserva")
            .setMessage(
                "Deseja cancelar a reserva da sala ${reserva.salaNome} em ${reserva.data}, ${reserva.periodoAula}ª aula?"
            )
            .setPositiveButton("Sim, cancelar") { _, _ ->
                cancelarReserva(reserva.id)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun cancelarReserva(reservaId: String) {
        mostrarErro(null)

        RetrofitClient.api.cancelarReserva(reservaId)
            .enqueue(object : Callback<ReservaResponse> {

                override fun onResponse(
                    call: Call<ReservaResponse>,
                    response: Response<ReservaResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@MinhasReservasActivity,
                            "Reserva cancelada com sucesso.",
                            Toast.LENGTH_LONG
                        ).show()

                        carregarMinhasReservas()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
                    mostrarErro("Falha na conexão ao cancelar reserva.")
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

            RetrofitClient.token = null
            finish()
        } else {
            mostrarErro(erro ?: "Erro no servidor: $codigo")
        }
    }
}