package com.example.agend.professor

import androidx.core.view.ViewCompat
import com.example.agend.utils.AcessibilidadeUtils
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
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

    // Armazena as reservas retornadas pelo backend.
    private val reservas = mutableListOf<ReservaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de reservas do usuário.
        setContentView(R.layout.activity_minhas_reservas)

        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        // Configura o retorno para a tela anterior.
        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        // Anuncia o retorno como botão para o TalkBack.
        AcessibilidadeUtils.marcarComoBotao(layoutVoltarTopo)

        // Define o título da tela para navegação por títulos no TalkBack.
        ViewCompat.setAccessibilityHeading(
            findViewById<TextView>(R.id.textoTituloMinhasReservas),
            true
        )

        listaReservas = findViewById(R.id.listaMinhasReservas)
        // Permite rolar a lista sem interferência do ScrollView pai.
        listaReservas.setOnTouchListener { view, _ ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        textoErro = findViewById(R.id.textoErroMinhasReservas)
        botaoAtualizar = findViewById(R.id.botaoAtualizarReservas)

        // Anuncia automaticamente as mensagens de erro da tela.
        ViewCompat.setAccessibilityLiveRegion(
            textoErro,
            ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
        )

        // Permite atualizar manualmente a lista de reservas.
        botaoAtualizar.setOnClickListener {
            carregarMinhasReservas()
        }

        carregarMinhasReservas()
    }

    private val conta by lazy { SessionManager(this).getEmail() }
    private fun carregarMinhasReservas() {
        mostrarErro(null)

        botaoAtualizar.isEnabled = false
        botaoAtualizar.text = "Carregando..."

        // Solicita ao backend as reservas do usuário autenticado.
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

                        com.example.agend.ReservaLocal(this@MinhasReservasActivity).use { it.salvar(conta, resposta) }
                        exibir(resposta)
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

                    val copia = com.example.agend.ReservaLocal(this@MinhasReservasActivity).use { it.ler(conta) }
                    if (copia != null) exibir(copia)
                    mostrarErro(if (copia == null) "Sem conexão. Nenhuma consulta salva." else "Sem conexão. Exibindo a última consulta salva.")

                }
            })
    }

    private fun exibir(resposta: List<ReservaResponse>) {
        reservas.clear()
        reservas.addAll(resposta)

        if (reservas.isEmpty()) {
            // Exibe uma mensagem quando não existem reservas cadastradas.
            listaReservas.adapter = ArrayAdapter(
                this@MinhasReservasActivity,
                R.layout.item_lista_vazia,
                listOf("Você ainda não possui reservas.")
            )
        } else {
            // Exibe as reservas utilizando o adapter personalizado.
            listaReservas.adapter = MinhasReservasAdapter(
                context = this@MinhasReservasActivity,
                reservas = reservas,
                onCancelarClick = { reserva ->
                    confirmarCancelamento(reserva)
                }
            )
        }

        // Informa o resultado da consulta para quem usa o TalkBack.
        listaReservas.announceForAccessibility(
            when (reservas.size) {
                0 -> "Você ainda não possui reservas"
                1 -> "1 reserva carregada"
                else -> "${reservas.size} reservas carregadas"
            }
        )
    }

    private fun confirmarCancelamento(reserva: ReservaResponse) {
        if (reserva.id.isNullOrBlank()) {
            mostrarErro("Reserva inválida.")
            return
        }

        // Define um texto padrão para reservas antigas sem turma informada.
        val turma = if (reserva.turma.isBlank()) {
            "não informada"
        } else {
            reserva.turma
        }

        // Solicita a confirmação antes de cancelar a reserva.
        AlertDialog.Builder(this)
            .setTitle("Cancelar reserva")
            .setMessage(
                "Deseja cancelar a reserva da sala ${reserva.salaNome}, turma $turma, em ${AcessibilidadeUtils.falarData(reserva.data)}, ${reserva.periodoAula}ª aula?"
            )
            .setPositiveButton("Sim, cancelar") { _, _ ->
                cancelarReserva(reserva.id)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun cancelarReserva(reservaId: String) {
        mostrarErro(null)

        // Envia ao backend a solicitação de cancelamento da reserva.
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

                        // Atualiza a lista após o cancelamento.
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

            // Remove o token da sessão em memória.
            RetrofitClient.token = null

            // Remove os dados da sessão persistida.
            SessionManager(this).limparSessao()

            // Retorna ao login e remove as telas anteriores da pilha.
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