package com.example.agend.professor

import com.example.agend.utils.NarradorAcessibilidade
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import com.example.agend.utils.AcessibilidadeUtils
import java.text.SimpleDateFormat
import java.util.Locale
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
import com.example.agend.auth.OpcaoUsoResponse
import android.widget.LinearLayout

class ReservarSalaActivity : AppCompatActivity() {

    private lateinit var spinnerSalas: Spinner
    private lateinit var spinnerTurnoReserva: Spinner
    private lateinit var spinnerOpcoesUso: Spinner
    private lateinit var layoutDataReserva: TextInputLayout
    private lateinit var editDataReserva: TextInputEditText
    private lateinit var layoutTurmaReserva: TextInputLayout
    private lateinit var editTurmaReserva: TextInputEditText
    private lateinit var botaoConsultar: Button
    private lateinit var textoErro: TextView
    private lateinit var textoTituloHorarios: TextView
    private lateinit var listaHorarios: ListView
    private lateinit var narrador: NarradorAcessibilidade

    private val salas = mutableListOf<SalaResponse>()
    private val nomesSalas = mutableListOf<String>()
    private val opcoesUso = mutableListOf<OpcaoUsoResponse>()
    private val nomesOpcoesUso = mutableListOf<String>()

    private val disponibilidades = mutableListOf<DisponibilidadeSalaResponse>()

    private lateinit var salasAdapter: ArrayAdapter<String>
    private lateinit var opcoesUsoAdapter: ArrayAdapter<String>
    private lateinit var turnoAdapter: ArrayAdapter<String>

    private var salaSelecionada: SalaResponse? = null
    private var opcaoUsoSelecionada: OpcaoUsoResponse? = null
    private var turnoSelecionado: String = "MANHA"
    private var dataSelecionada: String = ""

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

                    val imm =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
                // Nenhuma ação necessária antes da alteração.
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Remove os erros enquanto o usuário corrige o campo.
                layout.error = null
                mostrarErro(null)
            }

            override fun afterTextChanged(s: Editable?) {
                // Nenhuma ação necessária após a alteração.
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de reserva de sala.
        setContentView(R.layout.activity_reservar_sala)

        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        // Anuncia o retorno como botão para o TalkBack.
        AcessibilidadeUtils.marcarComoBotao(layoutVoltarTopo)

        // Configura o retorno para a tela anterior.
        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        spinnerSalas = findViewById(R.id.spinnerSalas)
        spinnerTurnoReserva = findViewById(R.id.spinnerTurnoReserva)
        spinnerOpcoesUso = findViewById(R.id.spinnerOpcoesUso)
        layoutDataReserva = findViewById(R.id.layoutDataReserva)
        editDataReserva = findViewById(R.id.editDataReserva)
        layoutTurmaReserva = findViewById(R.id.layoutTurmaReserva)
        editTurmaReserva = findViewById(R.id.editTurmaReserva)
        botaoConsultar = findViewById(R.id.botaoConsultarDisponibilidade)
        textoErro = findViewById(R.id.textoErroReserva)
        textoTituloHorarios = findViewById(R.id.textoTituloHorarios)
        listaHorarios = findViewById(R.id.listaHorariosDisponiveis)

        // Define os títulos da tela para navegação por títulos no TalkBack.
        ViewCompat.setAccessibilityHeading(
            findViewById<TextView>(R.id.textoTituloReserva),
            true
        )
        ViewCompat.setAccessibilityHeading(textoTituloHorarios, true)

        // Anuncia automaticamente as mensagens de erro da reserva.
        ViewCompat.setAccessibilityLiveRegion(
            textoErro,
            ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
        )

        // Inicializa a narração e avisa quando a voz em português não está disponível.
        narrador = NarradorAcessibilidade(this) {
            Toast.makeText(
                this,
                "Voz em português indisponível neste aparelho.",
                Toast.LENGTH_LONG
            ).show()
        }

        // Narra as informações da sala selecionada ao tocar no botão.
        findViewById<Button>(R.id.botaoOuvirSala).setOnClickListener {
            val sala = salaSelecionada

            if (sala == null) {
                narrador.falar("Nenhuma sala selecionada.")
            } else {
                narrador.falar(
                    "${sala.nomeEspaco}. Localização ${sala.localizacao}. " +
                            "Sala número ${sala.numeroSala}."
                )
            }
        }

        // Permite rolar a lista de horários sem interferência do ScrollView pai.
        listaHorarios.setOnTouchListener { view, _ ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        // Remove automaticamente os erros quando os campos são corrigidos.
        configurarLimpezaErro(editDataReserva, layoutDataReserva)
        configurarLimpezaErro(editTurmaReserva, layoutTurmaReserva)

        configurarAdapters()
        configurarEventos()
        carregarSalas()
        carregarOpcoesUso()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Libera o motor de voz ao fechar a tela.
        narrador.liberar()
    }

    private fun configurarAdapters() {
        // Configura o adapter das salas disponíveis.
        salasAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_sala,
            nomesSalas
        )

        salasAdapter.setDropDownViewResource(R.layout.item_spinner_sala_dropdown)
        spinnerSalas.adapter = salasAdapter

        // Configura o adapter dos turnos disponíveis.
        turnoAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_sala,
            listOf("Manhã", "Tarde", "Noite")
        )

        turnoAdapter.setDropDownViewResource(R.layout.item_spinner_sala_dropdown)
        spinnerTurnoReserva.adapter = turnoAdapter

        // Configura o adapter das finalidades de uso cadastradas.
        opcoesUsoAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_sala,
            nomesOpcoesUso
        )

        opcoesUsoAdapter.setDropDownViewResource(R.layout.item_spinner_sala_dropdown)
        spinnerOpcoesUso.adapter = opcoesUsoAdapter
    }

    private fun carregarOpcoesUso() {
        mostrarErro(null)

        // Busca no backend as finalidades de uso ativas.
        RetrofitClient.api.listarOpcoesUsoAtivas()
            .enqueue(object : Callback<List<OpcaoUsoResponse>> {

                override fun onResponse(
                    call: Call<List<OpcaoUsoResponse>>,
                    response: Response<List<OpcaoUsoResponse>>
                ) {
                    if (response.isSuccessful) {
                        val resposta = response.body() ?: emptyList()

                        opcoesUso.clear()
                        nomesOpcoesUso.clear()

                        opcoesUso.addAll(resposta)

                        if (opcoesUso.isEmpty()) {
                            nomesOpcoesUso.add("Nenhuma opção cadastrada")
                            spinnerOpcoesUso.isEnabled = false
                            botaoConsultar.isEnabled = false
                        } else {
                            nomesOpcoesUso.addAll(
                                opcoesUso.map { it.nome }
                            )

                            opcaoUsoSelecionada = opcoesUso.firstOrNull()
                            spinnerOpcoesUso.isEnabled = true
                            botaoConsultar.isEnabled = true
                        }

                        opcoesUsoAdapter.notifyDataSetChanged()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<OpcaoUsoResponse>>,
                    t: Throwable
                ) {
                    mostrarErro("Falha na conexão ao carregar opções de uso.")
                }
            })
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

                // Limpa os horários da sala anteriormente selecionada.
                limparHorarios()

                // Atualiza automaticamente a disponibilidade para a nova sala.
                if (dataSelecionada.isNotBlank()) {
                    consultarDisponibilidade()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                salaSelecionada = null
            }
        }

        spinnerTurnoReserva.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    turnoSelecionado = when (position) {
                        0 -> "MANHA"
                        1 -> "TARDE"
                        2 -> "NOITE"
                        else -> "MANHA"
                    }

                    // Limpa os horários do turno anterior.
                    limparHorarios()

                    // Atualiza automaticamente a disponibilidade para o novo turno.
                    if (dataSelecionada.isNotBlank()) {
                        consultarDisponibilidade()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    turnoSelecionado = "MANHA"
                }
            }

        spinnerOpcoesUso.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Armazena a finalidade selecionada pelo usuário.
                    opcaoUsoSelecionada = opcoesUso.getOrNull(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    opcaoUsoSelecionada = null
                }
            }

        editDataReserva.setOnClickListener {
            abrirDatePicker()
        }

        botaoConsultar.setOnClickListener {
            consultarDisponibilidade()
        }

        listaHorarios.setOnItemClickListener { _, _, position, _ ->
            val disponibilidade =
                disponibilidades.getOrNull(position)
                    ?: return@setOnItemClickListener

            if (!disponibilidade.disponivel) {
                Toast.makeText(
                    this,
                    "Este horário já está ocupado.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnItemClickListener
            }

            confirmarReserva(disponibilidade)
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
                val mesFormatado =
                    (mesSelecionado + 1).toString().padStart(2, '0')

                val diaFormatado =
                    diaSelecionado.toString().padStart(2, '0')

                dataSelecionada =
                    "$anoSelecionado-$mesFormatado-$diaFormatado"

                editDataReserva.setText(dataSelecionada)

                // Remove o erro após a seleção de uma data válida.
                layoutDataReserva.error = null
                mostrarErro(null)

                val calendarioSelecionado = Calendar.getInstance()
                calendarioSelecionado.set(
                    anoSelecionado,
                    mesSelecionado,
                    diaSelecionado
                )

                // Lê a data em formato falado, em vez de ano-mês-dia.
                val dataFalada = SimpleDateFormat(
                    "d 'de' MMMM 'de' yyyy",
                    Locale("pt", "BR")
                ).format(calendarioSelecionado.time)

                editDataReserva.contentDescription = "Data da reserva: $dataFalada"

                val diaSemana =
                    calendarioSelecionado.get(Calendar.DAY_OF_WEEK)

                // Domingo não permite reservas.
                // Sábados são validados pelo backend conforme o calendário letivo.
                if (diaSemana == Calendar.SUNDAY) {
                    limparHorarios()
                    mostrarErro("Não é permitido reservar salas aos domingos.")
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

        // Impede a seleção de datas anteriores à atual.
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000

        dialog.show()
    }

    private fun carregarSalas() {
        mostrarErro(null)

        // Busca no backend as salas disponíveis para reserva.
        RetrofitClient.api.listarSalas()
            .enqueue(object : Callback<List<SalaResponse>> {

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
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<SalaResponse>>,
                    t: Throwable
                ) {
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

        // Consulta ao backend os horários disponíveis para a sala, data e turno.
        RetrofitClient.api.consultarDisponibilidade(
            salaId = sala.id,
            data = dataSelecionada,
            turno = turnoSelecionado
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

                    // Exibe os horários no adapter personalizado.
                    listaHorarios.adapter = HorarioReservaAdapter(
                        this@ReservarSalaActivity,
                        disponibilidades
                    )

                    textoTituloHorarios.visibility = View.VISIBLE
                    // Informa quantos horários estão livres após cada consulta.
                    val livres = disponibilidades.count { it.disponivel }
                    listaHorarios.announceForAccessibility(
                        "Horários atualizados: $livres disponíveis de ${disponibilidades.size}"
                    )
                    listaHorarios.visibility = View.VISIBLE
                } else {
                    tratarErroSessaoOuServidor(
                        response.code(),
                        response.errorBody()?.string()
                    )
                }
            }

            override fun onFailure(
                call: Call<List<DisponibilidadeSalaResponse>>,
                t: Throwable
            ) {
                botaoConsultar.isEnabled = true
                botaoConsultar.text = "Atualizar disponibilidade"

                mostrarErro("Falha na conexão ao consultar disponibilidade.")
            }
        })
    }

    // Solicita a confirmação antes de criar a reserva, evitando toques acidentais.
    private fun confirmarReserva(disponibilidade: DisponibilidadeSalaResponse) {
        val sala = salaSelecionada ?: return

        // Garante que a turma esteja preenchida antes de abrir a confirmação.
        if (editTurmaReserva.text.toString().isBlank()) {
            layoutTurmaReserva.error = "Informe a turma que usará o espaço"
            mostrarErro("Informe a turma antes de reservar.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar reserva")
            .setMessage(
                "Deseja reservar ${sala.nomeEspaco} em " +
                        "${AcessibilidadeUtils.falarData(dataSelecionada)}, " +
                        "${disponibilidade.periodoAula}ª aula, " +
                        "das ${AcessibilidadeUtils.falarHorario(disponibilidade.horarioInicio)} " +
                        "às ${AcessibilidadeUtils.falarHorario(disponibilidade.horarioFim)}?"
            )
            .setPositiveButton("Sim, reservar") { _, _ ->
                criarReserva(disponibilidade)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun criarReserva(disponibilidade: DisponibilidadeSalaResponse) {
        val sala = salaSelecionada

        if (sala == null || sala.id.isNullOrBlank()) {
            mostrarErro("Selecione uma sala válida.")
            return
        }

        val opcaoUso = opcaoUsoSelecionada

        if (opcaoUso == null || opcaoUso.id.isNullOrBlank()) {
            mostrarErro("Selecione a finalidade de uso do espaço.")
            return
        }

        // Obtém a turma informada pelo professor.
        val turma = editTurmaReserva.text.toString().trim()

        // Remove o erro anterior antes de validar novamente.
        layoutTurmaReserva.error = null

        // A turma identifica a classe que utilizará o espaço.
        if (turma.isBlank()) {
            layoutTurmaReserva.error = "Informe a turma que usará o espaço"
            mostrarErro("Informe a turma antes de reservar.")
            return
        }

        val request = ReservaRequest(
            salaId = sala.id,
            data = dataSelecionada,
            periodoAula = disponibilidade.periodoAula,
            turma = turma,
            opcaoUsoId = opcaoUso.id
        )

        // Envia a solicitação de reserva ao backend.
        RetrofitClient.api.criarReserva(request)
            .enqueue(object : Callback<ReservaResponse> {

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

                        // Atualiza a disponibilidade após a reserva.
                        consultarDisponibilidade()
                    } else {
                        val erroServidor = response.errorBody()?.string()

                        // Trata especificamente o conflito de horário.
                        if (response.code() == 409) {
                            val mensagem = erroServidor
                                ?: "ERRO: Esta sala já está reservada neste dia e horário."

                            mostrarErro(mensagem)

                            Toast.makeText(
                                this@ReservarSalaActivity,
                                "Esta sala já está reservada neste horário.",
                                Toast.LENGTH_LONG
                            ).show()

                            // Atualiza a disponibilidade para refletir o conflito.
                            consultarDisponibilidade()
                            return
                        }

                        tratarErroSessaoOuServidor(
                            response.code(),
                            erroServidor
                        )
                    }
                }

                override fun onFailure(
                    call: Call<ReservaResponse>,
                    t: Throwable
                ) {
                    // Trata falhas sem resposta do servidor, como timeout ou falta de conexão.
                    mostrarErro("Falha na conexão ao criar reserva.")
                }
            })
    }

    private fun limparHorarios() {
        disponibilidades.clear()

        // Remove os horários atualmente exibidos.
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