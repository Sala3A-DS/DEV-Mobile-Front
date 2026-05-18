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
import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.agend.MainActivity
import com.example.agend.auth.SessionManager
import com.example.agend.professor.adapter.HorarioReservaAdapter
import android.text.Editable
import android.text.TextWatcher

class ReservarSalaActivity : AppCompatActivity() {

    private lateinit var spinnerSalas: Spinner
    private lateinit var layoutDataReserva: TextInputLayout
    private lateinit var editDataReserva: TextInputEditText
    private lateinit var layoutTurmaReserva: TextInputLayout
    private lateinit var editTurmaReserva: TextInputEditText
    private lateinit var botaoConsultar: Button
    private lateinit var textoErro: TextView
    private lateinit var textoTituloHorarios: TextView
    private lateinit var listaHorarios: ListView
    private lateinit var textoVoltar: TextView

    private val salas = mutableListOf<SalaResponse>()
    private val nomesSalas = mutableListOf<String>()

    private val disponibilidades = mutableListOf<DisponibilidadeSalaResponse>()

    private lateinit var salasAdapter: ArrayAdapter<String>

    private var salaSelecionada: SalaResponse? = null
    private var dataSelecionada: String = ""

    //Sair da funcao do teclado
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val viewAtual = currentFocus

            // Se o foco atual estiver em um campo de texto, verifica se o toque foi fora dele.
            if (viewAtual is EditText) {
                val areaDoCampo = android.graphics.Rect()
                viewAtual.getGlobalVisibleRect(areaDoCampo)

                val tocouForaDoCampo = !areaDoCampo.contains(
                    event.rawX.toInt(),
                    event.rawY.toInt()
                )

                if (tocouForaDoCampo) {
                    // Remove o foco do campo.
                    viewAtual.clearFocus()

                    // Esconde o teclado.
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewAtual.windowToken, 0)
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

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
            ) {
                // Não é necessário tratar antes da mudança.
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Remove o erro visual quando o usuário começa a corrigir o campo.
                // Isso faz o label voltar para a cor normal.
                layout.error = null
                mostrarErro(null)
            }

            override fun afterTextChanged(s: Editable?) {
                // Não é necessário tratar depois da mudança.
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reservar_sala)

        spinnerSalas = findViewById(R.id.spinnerSalas)
        layoutDataReserva = findViewById(R.id.layoutDataReserva)
        editDataReserva = findViewById(R.id.editDataReserva)
        layoutTurmaReserva = findViewById(R.id.layoutTurmaReserva)
        editTurmaReserva = findViewById(R.id.editTurmaReserva)
        botaoConsultar = findViewById(R.id.botaoConsultarDisponibilidade)
        textoErro = findViewById(R.id.textoErroReserva)
        textoTituloHorarios = findViewById(R.id.textoTituloHorarios)
        listaHorarios = findViewById(R.id.listaHorariosDisponiveis)
        textoVoltar = findViewById(R.id.textoVoltarReserva)

        // Limpa automaticamente os erros dos campos quando o usuário altera o conteúdo.
        // Isso evita que o label continue vermelho depois que o campo foi corrigido.
        configurarLimpezaErro(editDataReserva, layoutDataReserva)
        configurarLimpezaErro(editTurmaReserva, layoutTurmaReserva)

        configurarAdapters()
        configurarEventos()
        carregarSalas()
    }

    private fun configurarAdapters() {
        // Usa layout próprio para impedir que o Android use texto preto no Spinner.
        salasAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_sala,
            nomesSalas
        )

        // Layout próprio também na lista aberta do Spinner.
        salasAdapter.setDropDownViewResource(R.layout.item_spinner_sala_dropdown)

        spinnerSalas.adapter = salasAdapter
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

                // Se já tiver uma data selecionada, consulta automaticamente
                // a disponibilidade da nova sala para essa mesma data.
                if (dataSelecionada.isNotBlank()) {
                    consultarDisponibilidade()
                }
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

                dataSelecionada = "$anoSelecionado-$mesFormatado-$diaFormatado"
                editDataReserva.setText(dataSelecionada)

                // Remove erro visual da data depois que o usuário escolhe uma data válida.
                layoutDataReserva.error = null
                mostrarErro(null)

                // Verifica se a data escolhida é sábado ou domingo antes de consultar.
                val calendarioSelecionado = Calendar.getInstance()
                calendarioSelecionado.set(anoSelecionado, mesSelecionado, diaSelecionado)

                val diaSemana = calendarioSelecionado.get(Calendar.DAY_OF_WEEK)

                if (
                    diaSemana == Calendar.SATURDAY ||
                    diaSemana == Calendar.SUNDAY
                ) {
                    limparHorarios()
                    mostrarErro("Não é permitido reservar salas aos finais de semana.")
                    return@DatePickerDialog
                }

                limparHorarios()

                val sala = salaSelecionada

                if (sala != null && !sala.id.isNullOrBlank()) {
                    consultarDisponibilidade()
                }
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
                                "${it.nomeEspaco} - ${it.localizacao} (Sala ${it.numeroSala})"
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
                botaoConsultar.text = "Atualizar disponibilidade"

                if (response.isSuccessful) {
                    val resposta = response.body() ?: emptyList()

                    disponibilidades.clear()
                    disponibilidades.addAll(resposta)

                    // Usa adapter customizado para mostrar os horários em cards.
                    listaHorarios.adapter = HorarioReservaAdapter(
                        this@ReservarSalaActivity,
                        disponibilidades
                    )

                    textoTituloHorarios.visibility = View.VISIBLE
                    listaHorarios.visibility = View.VISIBLE
                } else {
                    tratarErroSessaoOuServidor(response.code(), response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<List<DisponibilidadeSalaResponse>>, t: Throwable) {
                botaoConsultar.isEnabled = true
                botaoConsultar.text = "Atualizar disponibilidade"
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

        // Lê a turma informada pelo professor.
        // Exemplo: 3A - DS.
        val turma = editTurmaReserva.text.toString().trim()

        // Limpa erro antigo do campo turma.
        layoutTurmaReserva.error = null

        // A turma é obrigatória para identificar qual classe usará o espaço.
        if (turma.isBlank()) {
            layoutTurmaReserva.error = "Informe a turma que usará o espaço"
            mostrarErro("Informe a turma antes de reservar.")
            return
        }

        val request = ReservaRequest(
            salaId = sala.id,
            data = dataSelecionada,
            periodoAula = disponibilidade.periodoAula,
            turma = turma
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
                    val erroServidor = response.errorBody()?.string()

                    // Caso específico: outra pessoa já reservou essa sala nesse horário.
                    // O back-end deve retornar 409 Conflict nesse cenário.
                    if (response.code() == 409) {
                        val mensagem = erroServidor
                            ?: "ERRO: Esta sala já está reservada neste dia e horário."

                        mostrarErro(mensagem)

                        Toast.makeText(
                            this@ReservarSalaActivity,
                            "Esta sala já está reservada neste horário.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Atualiza a disponibilidade para mostrar o horário como ocupado.
                        consultarDisponibilidade()
                        return
                    }

                    // Outros erros continuam sendo tratados pelo método geral.
                    tratarErroSessaoOuServidor(response.code(), erroServidor)
                }
            }

            override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
                // onFailure só deve acontecer quando não houve resposta do servidor:
                // sem internet, timeout, servidor fora, etc.
                mostrarErro("Falha na conexão ao criar reserva.")
            }
        })
    }
    private fun limparHorarios() {
        disponibilidades.clear()

        // Remove os cards antigos da lista.
        listaHorarios.adapter = null

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

            // Limpa o token em memória.
            RetrofitClient.token = null

            // Limpa a sessão salva no celular.
            SessionManager(this).limparSessao()

            // Volta para a tela de login e impede o usuário de voltar para telas protegidas.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } else {
            mostrarErro(erro ?: "Erro no servidor: $codigo")
        }
    }
}