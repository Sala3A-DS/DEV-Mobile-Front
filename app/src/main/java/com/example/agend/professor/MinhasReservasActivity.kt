package com.example.agend.professor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.ReservaResponse
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.professor.adapter.MinhasReservasAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MinhasReservasActivity : AppCompatActivity() {

    private lateinit var listaReservas: ListView
    private lateinit var textoErro: TextView
    private lateinit var botaoAtualizar: Button
    private lateinit var textoVoltar: TextView

    // Lista com as reservas retornadas pelo back-end.
    private val reservas = mutableListOf<ReservaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela de minhas reservas.
        setContentView(R.layout.activity_minhas_reservas)

        listaReservas = findViewById(R.id.listaMinhasReservas)
        textoErro = findViewById(R.id.textoErroMinhasReservas)
        botaoAtualizar = findViewById(R.id.botaoAtualizarReservas)
        textoVoltar = findViewById(R.id.textoVoltarMinhasReservas)

        // Atualiza manualmente a lista de reservas.
        botaoAtualizar.setOnClickListener {
            carregarMinhasReservas()
        }

        // Volta para a tela anterior.
        textoVoltar.setOnClickListener {
            finish()
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
                        reservas.addAll(resposta)

                        if (reservas.isEmpty()) {
                            // Estado vazio: mostra uma mensagem simples quando não há reservas.
                            listaReservas.adapter = ArrayAdapter(
                                this@MinhasReservasActivity,
                                android.R.layout.simple_list_item_1,
                                listOf("Você ainda não possui reservas.")
                            )
                        } else {
                            // Usa o adapter customizado para exibir cada reserva em formato de card.
                            listaReservas.adapter = MinhasReservasAdapter(
                                context = this@MinhasReservasActivity,
                                reservas = reservas,
                                onCancelarClick = { reserva ->
                                    confirmarCancelamento(reserva)
                                }
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

                    mostrarErro("Falha na conexão ao carregar reservas.")
                }
            })
    }

    private fun confirmarCancelamento(reserva: ReservaResponse) {
        if (reserva.id.isNullOrBlank()) {
            mostrarErro("Reserva inválida.")
            return
        }

        // Se a turma vier vazia por alguma reserva antiga, mostra um texto padrão.
        val turma = if (reserva.turma.isBlank()) {
            "não informada"
        } else {
            reserva.turma
        }

        AlertDialog.Builder(this)
            .setTitle("Cancelar reserva")
            .setMessage(
                "Deseja cancelar a reserva da sala ${reserva.salaNome}, turma $turma, em ${reserva.data}, ${reserva.periodoAula}ª aula?"
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

                        // Recarrega a lista para remover/atualizar a reserva cancelada.
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