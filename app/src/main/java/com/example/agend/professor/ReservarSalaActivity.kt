package com.example.agend.professor

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.DisponibilidadeSalaResponse
import com.example.agend.auth.ReservaRequest
import com.example.agend.auth.ReservaResponse
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SalaResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import com.example.agend.R

class ReservarSalaActivity : AppCompatActivity() {

    private lateinit var spinnerSalas: Spinner
    private lateinit var layoutDataReserva: TextInputLayout
    private lateinit var editDataReserva: TextInputEditText
    private lateinit var botaoConsultar: Button
    private lateinit var textoErro: TextView
    private lateinit var textoTituloHorarios: TextView
    private lateinit var listaHorarios: ListView
    private lateinit var textoVoltar: TextView

    private val salas = mutableListOf<SalaResponse>()
    private val nomesSalas = mutableListOf<String>()

    private val disponibilidades = mutableListOf<DisponibilidadeSalaResponse>()
    private val horariosFormatados = mutableListOf<String>()

    private lateinit var salasAdapter: ArrayAdapter<String>
    private lateinit var horariosAdapter: ArrayAdapter<String>

    private var salaSelecionada: SalaResponse? = null
    private var dataSelecionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reservar_sala)

        spinnerSalas = findViewById(R.id.spinnerSalas)
        layoutDataReserva = findViewById(R.id.layoutDataReserva)
        editDataReserva = findViewById(R.id.editDataReserva)
        botaoConsultar = findViewById(R.id.botaoConsultarDisponibilidade)
        textoErro = findViewById(R.id.textoErroReserva)
        textoTituloHorarios = findViewById(R.id.textoTituloHorarios)
        listaHorarios = findViewById(R.id.listaHorariosDisponiveis)
        textoVoltar = findViewById(R.id.textoVoltarReserva)

        configurarAdapters()
        configurarEventos()
        carregarSalas()
    }

    private fun configurarAdapters() {
        salasAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nomesSalas
        )
        salasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSalas.adapter = salasAdapter

        horariosAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            horariosFormatados
        )
        listaHorarios.adapter = horariosAdapter
    }

    private fun configurarEventos() {
        spinnerSalas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                salaSelecionada = salas.getOrNull(position)

                // Quando muda a sala, limpamos os horários antigos.
                limparHorarios()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                salaSelecionada = null
            }
        }

        editDataReserva.setOnClickListener {
            abrirDatePicker()
        }

        botaoConsultar.setOnClickListener {
            consultarDisponibilidade()
        }

        listaHorarios.setOnItemClickListener { _, _, position, _ ->
            val disponibilidade = disponibilidades.getOrNull(position) ?: return@setOnItemClickListener

            if (!disponibilidade.disponivel) {
                Toast.makeText(
                    this,
                    "Este horário já está ocupado.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnItemClickListener
            }

            criarReserva(disponibilidade)
        }

        textoVoltar.setOnClickListener {
            finish()
        }
    }

    private fun abrirDatePicker() {
        val calendario = Calendar.getInstance()

        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            this,
            { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                val mesFormatado = (mesSelecionado + 1).toString().padStart(2, '0')
                val diaFormatado = diaSelecionado.toString().padStart(2, '0')

                // Formato esperado pelo back-end: yyyy-MM-dd
                dataSelecionada = "$anoSelecionado-$mesFormatado-$diaFormatado"
                editDataReserva.setText(dataSelecionada)

                limparHorarios()
            },
            ano,
            mes,
            dia
        )

        // Impede selecionar datas passadas.
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000

        dialog.show()
    }

    private fun carregarSalas() {
        mostrarErro(null)

        RetrofitClient.api.listarSalas().enqueue(object : Callback<List<SalaResponse>> {
            override fun onResponse(
                call: Call<List<SalaResponse>>,
                response: Response<List<SalaResponse>>
            ) {
                if (response.isSuccessful) {
                    val resposta = response.body() ?: emptyList()

                    salas.clear()
                    nomesSalas.clear()

                    salas.addAll(resposta)

                    if (salas.isEmpty()) {
                        nomesSalas.add("Nenhuma sala cadastrada")
                        spinnerSalas.isEnabled = false
                        botaoConsultar.isEnabled = false
                    } else {
                        nomesSalas.addAll(
                            salas.map {
                                "${it.nome} - ${it.bloco} (${it.capacidade} lugares)"
                            }
                        )

                        spinnerSalas.isEnabled = true
                        botaoConsultar.isEnabled = true
                    }

                    salasAdapter.notifyDataSetChanged()
                } else {
                    tratarErroSessaoOuServidor(response.code(), response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<List<SalaResponse>>, t: Throwable) {
                mostrarErro("Falha na conexão ao carregar salas.")
            }
        })
    }

    private fun consultarDisponibilidade() {
        mostrarErro(null)

        val sala = salaSelecionada

        if (sala == null || sala.id.isNullOrBlank()) {
            mostrarErro("Selecione uma sala válida.")
            return
        }

        if (dataSelecionada.isBlank()) {
            layoutDataReserva.error = "Selecione uma data"
            return
        }

        layoutDataReserva.error = null

        botaoConsultar.isEnabled = false
        botaoConsultar.text = "Consultando..."

        RetrofitClient.api.consultarDisponibilidade(
            salaId = sala.id,
            data = dataSelecionada
        ).enqueue(object : Callback<List<DisponibilidadeSalaResponse>> {
            override fun onResponse(
                call: Call<List<DisponibilidadeSalaResponse>>,
                response: Response<List<DisponibilidadeSalaResponse>>
            ) {
                botaoConsultar.isEnabled = true
                botaoConsultar.text = "Consultar disponibilidade"

                if (response.isSuccessful) {
                    val resposta = response.body() ?: emptyList()

                    disponibilidades.clear()
                    horariosFormatados.clear()

                    disponibilidades.addAll(resposta)

                    horariosFormatados.addAll(
                        resposta.map { item ->
                            val status = if (item.disponivel) {
                                "Disponível"
                            } else {
                                "Ocupado por ${item.professorNome ?: "outro professor"}"
                            }

                            "${item.periodoAula}ª aula - ${item.horarioInicio} às ${item.horarioFim}\n$status"
                        }
                    )

                    textoTituloHorarios.visibility = View.VISIBLE
                    listaHorarios.visibility = View.VISIBLE
                    horariosAdapter.notifyDataSetChanged()
                } else {
                    tratarErroSessaoOuServidor(response.code(), response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<List<DisponibilidadeSalaResponse>>, t: Throwable) {
                botaoConsultar.isEnabled = true
                botaoConsultar.text = "Consultar disponibilidade"
                mostrarErro("Falha na conexão ao consultar disponibilidade.")
            }
        })
    }

    private fun criarReserva(disponibilidade: DisponibilidadeSalaResponse) {
        val sala = salaSelecionada

        if (sala == null || sala.id.isNullOrBlank()) {
            mostrarErro("Selecione uma sala válida.")
            return
        }

        val request = ReservaRequest(
            salaId = sala.id,
            data = dataSelecionada,
            periodoAula = disponibilidade.periodoAula
        )

        RetrofitClient.api.criarReserva(request).enqueue(object : Callback<ReservaResponse> {
            override fun onResponse(
                call: Call<ReservaResponse>,
                response: Response<ReservaResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ReservarSalaActivity,
                        "Reserva criada com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()

                    // Atualiza a lista para mostrar o horário como ocupado.
                    consultarDisponibilidade()
                } else {
                    tratarErroSessaoOuServidor(response.code(), response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
                mostrarErro("Falha na conexão ao criar reserva.")
            }
        })
    }

    private fun limparHorarios() {
        disponibilidades.clear()
        horariosFormatados.clear()
        horariosAdapter.notifyDataSetChanged()

        textoTituloHorarios.visibility = View.GONE
        listaHorarios.visibility = View.GONE
    }

    private fun mostrarErro(mensagem: String?) {
        if (mensagem.isNullOrBlank()) {
            textoErro.visibility = View.GONE
            textoErro.text = ""
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