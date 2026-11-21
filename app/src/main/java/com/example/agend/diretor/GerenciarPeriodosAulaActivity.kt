package com.example.agend.diretor

import androidx.core.view.ViewCompat
import com.example.agend.utils.AcessibilidadeUtils
import com.example.agend.auth.GerarPeriodosRequest
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
import com.example.agend.auth.PeriodoAulaResponse
import com.example.agend.auth.PeriodoAulaStatusRequest
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.adapter.PeriodoAulaAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.text.Editable
import android.text.TextWatcher
import android.widget.Spinner
import android.widget.AdapterView
import android.widget.LinearLayout

class GerenciarPeriodosAulaActivity : AppCompatActivity() {

    private lateinit var listaPeriodos: ListView
    private lateinit var textoErro: TextView

    private lateinit var layoutQuantidade: TextInputLayout
    private lateinit var layoutHorarioInicialGeracao: TextInputLayout
    private lateinit var layoutDuracaoAula: TextInputLayout
    private lateinit var layoutIntervaloAposAula: TextInputLayout
    private lateinit var layoutDuracaoIntervalo: TextInputLayout

    private lateinit var editQuantidade: TextInputEditText
    private lateinit var editHorarioInicialGeracao: TextInputEditText
    private lateinit var editDuracaoAula: TextInputEditText
    private lateinit var editIntervaloAposAula: TextInputEditText
    private lateinit var editDuracaoIntervalo: TextInputEditText

    private lateinit var botaoGerarPeriodos: Button

    private lateinit var spinnerTurnoGeracao: Spinner
    private lateinit var turnoSelecionado: String

    private lateinit var botaoFiltroManha: Button
    private lateinit var botaoFiltroTarde: Button
    private lateinit var botaoFiltroNoite: Button

    private var turnoFiltroSelecionado: String = "MANHA"
    private val periodos = mutableListOf<PeriodoAulaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de gerenciamento dos períodos de aula.
        setContentView(R.layout.activity_gerenciar_periodos_aula)

        // Configura o botão de retorno para fechar a tela atual.
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }
        // Anuncia o retorno como botão para o TalkBack.
        AcessibilidadeUtils.marcarComoBotao(layoutVoltarTopo)

        // Define os títulos da tela para navegação por títulos no TalkBack.
        listOf(
            R.id.textoTituloPeriodos,
            R.id.textoSubtituloGerar,
            R.id.textoSubtituloHorarios
        ).forEach {
            ViewCompat.setAccessibilityHeading(findViewById<TextView>(it), true)
        }


        listaPeriodos = findViewById(R.id.listaPeriodosAula)
        textoErro = findViewById(R.id.textoErroPeriodosAula)

        // Anuncia automaticamente as mensagens de erro da tela.
        ViewCompat.setAccessibilityLiveRegion(
            textoErro,
            ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
        )

        // Permite rolar a lista sem que o ScrollView externo intercepte o toque.
        listaPeriodos.setOnTouchListener { view, _ ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        layoutQuantidade = findViewById(R.id.layoutQuantidadeAulas)
        layoutHorarioInicialGeracao = findViewById(R.id.layoutHorarioInicialGeracao)
        layoutDuracaoAula = findViewById(R.id.layoutDuracaoAula)
        layoutIntervaloAposAula = findViewById(R.id.layoutIntervaloAposAula)
        layoutDuracaoIntervalo = findViewById(R.id.layoutDuracaoIntervalo)

        editQuantidade = findViewById(R.id.editQuantidadeAulas)
        editHorarioInicialGeracao = findViewById(R.id.editHorarioInicialGeracao)
        editDuracaoAula = findViewById(R.id.editDuracaoAula)
        editIntervaloAposAula = findViewById(R.id.editIntervaloAposAula)
        editDuracaoIntervalo = findViewById(R.id.editDuracaoIntervalo)

        // Remove automaticamente os erros durante a edição dos campos.
        configurarLimpezaErro(editQuantidade, layoutQuantidade)
        configurarLimpezaErro(editHorarioInicialGeracao, layoutHorarioInicialGeracao)
        configurarLimpezaErro(editDuracaoAula, layoutDuracaoAula)
        configurarLimpezaErro(editIntervaloAposAula, layoutIntervaloAposAula)
        configurarLimpezaErro(editDuracaoIntervalo, layoutDuracaoIntervalo)

        botaoGerarPeriodos = findViewById(R.id.botaoGerarPeriodosAula)

        botaoFiltroManha = findViewById(R.id.botaoFiltroManha)
        botaoFiltroTarde = findViewById(R.id.botaoFiltroTarde)
        botaoFiltroNoite = findViewById(R.id.botaoFiltroNoite)

        // Filtra os períodos pelo turno selecionado.
        botaoFiltroManha.setOnClickListener {
            turnoFiltroSelecionado = "MANHA"
            atualizarBotoesFiltro()
            atualizarLista()
        }

        botaoFiltroTarde.setOnClickListener {
            turnoFiltroSelecionado = "TARDE"
            atualizarBotoesFiltro()
            atualizarLista()
        }

        botaoFiltroNoite.setOnClickListener {
            turnoFiltroSelecionado = "NOITE"
            atualizarBotoesFiltro()
            atualizarLista()
        }

        spinnerTurnoGeracao = findViewById(R.id.spinnerTurnoGeracao)

        val nomesTurnos = listOf("Manhã", "Tarde", "Noite")
        val valoresTurnos = listOf("MANHA", "TARDE", "NOITE")

        val turnoAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_sala,
            nomesTurnos
        )

        turnoAdapter.setDropDownViewResource(R.layout.item_spinner_sala_dropdown)
        spinnerTurnoGeracao.adapter = turnoAdapter

        turnoSelecionado = valoresTurnos.first()

        // Atualiza o turno utilizado na geração automática.
        spinnerTurnoGeracao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                turnoSelecionado = valoresTurnos[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                turnoSelecionado = "MANHA"
            }
        }

        // Inicia a geração automática dos períodos.
        botaoGerarPeriodos.setOnClickListener {
            gerarPeriodosAutomaticamente()
        }

        atualizarBotoesFiltro()
        carregarPeriodos()
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
                // Remove o erro enquanto o usuário corrige o campo.
                layout.error = null
                mostrarErro(null)
            }

            override fun afterTextChanged(s: Editable?) {
                // Nenhuma ação necessária após a alteração.
            }
        })
    }

    private fun atualizarBotoesFiltro() {
        botaoFiltroManha.setBackgroundColor(
            getColor(
                if (turnoFiltroSelecionado == "MANHA")
                    R.color.yarooms_gold
                else
                    R.color.yarooms_gold_light
            )
        )

        botaoFiltroTarde.setBackgroundColor(
            getColor(
                if (turnoFiltroSelecionado == "TARDE")
                    R.color.yarooms_gold
                else
                    R.color.yarooms_gold_light
            )
        )

        botaoFiltroNoite.setBackgroundColor(
            getColor(
                if (turnoFiltroSelecionado == "NOITE")
                    R.color.yarooms_gold
                else
                    R.color.yarooms_gold_light
            )
        )

        // Informa o filtro selecionado, já que a seleção é indicada apenas pela cor.
        listOf(
            Triple("MANHA", "Manhã", botaoFiltroManha),
            Triple("TARDE", "Tarde", botaoFiltroTarde),
            Triple("NOITE", "Noite", botaoFiltroNoite)
        ).forEach { (turno, nome, botao) ->
            botao.contentDescription =
                if (turnoFiltroSelecionado == turno) "$nome, selecionado" else "$nome, não selecionado"
        }
    }

    // Informa o resultado do filtro para quem usa o TalkBack.
    private fun anunciarFiltro() {
        val turnoFalado = when (turnoFiltroSelecionado) {
            "MANHA" -> "manhã"
            "TARDE" -> "tarde"
            else -> "noite"
        }

        val total = periodos.count {
            it.turno.equals(turnoFiltroSelecionado, ignoreCase = true)
        }

        listaPeriodos.announceForAccessibility(
            "Turno $turnoFalado: " +
                    if (total == 0) "nenhum período cadastrado" else "$total períodos"
        )
    }

    private fun carregarPeriodos() {
        mostrarErro(null)

        // Consulta ao backend os períodos disponíveis para o administrador.
        RetrofitClient.api.listarPeriodosAulaAdmin()
            .enqueue(object : Callback<List<PeriodoAulaResponse>> {

                override fun onResponse(
                    call: Call<List<PeriodoAulaResponse>>,
                    response: Response<List<PeriodoAulaResponse>>
                ) {
                    if (response.isSuccessful) {
                        val resposta = response.body() ?: emptyList()

                        periodos.clear()
                        periodos.addAll(resposta)

                        atualizarLista()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<List<PeriodoAulaResponse>>, t: Throwable) {
                    mostrarErro("Falha na conexão ao carregar períodos.")
                }
            })
    }

    private fun atualizarLista() {
        // Exibe somente os períodos correspondentes ao turno selecionado.
        val periodosFiltrados = periodos.filter {
            it.turno.equals(turnoFiltroSelecionado, ignoreCase = true)
        }

        if (periodosFiltrados.isEmpty()) {
            val nomeTurno = when (turnoFiltroSelecionado) {
                "MANHA" -> "manhã"
                "TARDE" -> "tarde"
                "NOITE" -> "noite"
                else -> "selecionado"
            }

            listaPeriodos.adapter = ArrayAdapter(
                this,
                R.layout.item_lista_vazia,
                listOf("Nenhum período cadastrado para $nomeTurno.")
            )
            return
        }

        // Exibe os períodos utilizando o adapter personalizado.
        listaPeriodos.adapter = PeriodoAulaAdapter(
            context = this,
            periodos = periodosFiltrados
        ) { periodo ->
            alterarStatus(periodo)
        }
    }

    private fun gerarPeriodosAutomaticamente() {
        mostrarErro(null)
        limparErrosGeracao()

        val quantidadeTexto = editQuantidade.text.toString().trim()
        val horarioInicial = editHorarioInicialGeracao.text.toString().trim()
        val duracaoTexto = editDuracaoAula.text.toString().trim()
        val intervaloAposTexto = editIntervaloAposAula.text.toString().trim()
        val duracaoIntervaloTexto = editDuracaoIntervalo.text.toString().trim()

        val quantidade = quantidadeTexto.toIntOrNull()
        val duracaoAula = duracaoTexto.toIntOrNull()

        // Valida a quantidade de aulas informada.
        if (quantidade == null || quantidade <= 0) {
            layoutQuantidade.error = "Informe uma quantidade válida"
            return
        }

        // Valida o horário inicial no formato esperado pelo backend.
        if (!horarioInicial.matches(Regex("^\\d{2}:\\d{2}$"))) {
            layoutHorarioInicialGeracao.error = "Use o formato HH:mm"
            return
        }

        // Valida a duração de cada aula.
        if (duracaoAula == null || duracaoAula <= 0) {
            layoutDuracaoAula.error = "Informe uma duração válida"
            return
        }

        val intervaloApos = intervaloAposTexto.toIntOrNull()
        val duracaoIntervalo = duracaoIntervaloTexto.toIntOrNull()

        val request = GerarPeriodosRequest(
            turno = turnoSelecionado,
            quantidadeAulas = quantidade,
            horarioInicio = horarioInicial,
            duracaoMinutos = duracaoAula,
            intervaloAposAula = intervaloApos,
            duracaoIntervaloMinutos = duracaoIntervalo
        )

        // Evita múltiplas solicitações durante a geração.
        botaoGerarPeriodos.isEnabled = false
        botaoGerarPeriodos.text = "Gerando..."

        // Envia os parâmetros de geração automática para o backend.
        RetrofitClient.api.gerarPeriodosAula(request)
            .enqueue(object : Callback<List<PeriodoAulaResponse>> {

                override fun onResponse(
                    call: Call<List<PeriodoAulaResponse>>,
                    response: Response<List<PeriodoAulaResponse>>
                ) {
                    botaoGerarPeriodos.isEnabled = true
                    botaoGerarPeriodos.text = "Gerar períodos automaticamente"

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@GerenciarPeriodosAulaActivity,
                            "Períodos gerados com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Atualiza a lista após a geração.
                        carregarPeriodos()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<List<PeriodoAulaResponse>>, t: Throwable) {
                    botaoGerarPeriodos.isEnabled = true
                    botaoGerarPeriodos.text = "Gerar períodos automaticamente"

                    mostrarErro("Falha na conexão ao gerar períodos.")
                }
            })
    }

    private fun alterarStatus(periodo: PeriodoAulaResponse) {
        val id = periodo.id

        // Impede a alteração quando o período não possui identificador válido.
        if (id.isNullOrBlank()) {
            mostrarErro("Período inválido.")
            return
        }

        val request = PeriodoAulaStatusRequest(
            ativo = !periodo.ativo
        )

        // Atualiza o status do período no backend.
        RetrofitClient.api.alterarStatusPeriodoAula(id, request)
            .enqueue(object : Callback<PeriodoAulaResponse> {

                override fun onResponse(
                    call: Call<PeriodoAulaResponse>,
                    response: Response<PeriodoAulaResponse>
                ) {
                    if (response.isSuccessful) {
                        // Informa o novo estado, já que a lista é recriada após a alteração.
                        listaPeriodos.announceForAccessibility(
                            "${periodo.numero}ª aula ${if (periodo.ativo) "desativada" else "ativada"}"
                        )

                        // Recarrega a lista para refletir o novo status.
                        carregarPeriodos()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<PeriodoAulaResponse>, t: Throwable) {
                    mostrarErro("Falha na conexão ao alterar status.")
                }
            })
    }

    private fun limparErrosGeracao() {
        layoutQuantidade.error = null
        layoutHorarioInicialGeracao.error = null
        layoutDuracaoAula.error = null
        layoutIntervaloAposAula.error = null
        layoutDuracaoIntervalo.error = null
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

            // Retorna ao login e remove as telas protegidas da pilha.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Exibe a mensagem retornada pelo backend ou o código HTTP.
            mostrarErro(erro ?: "Erro no servidor: $codigo")
        }
    }
}