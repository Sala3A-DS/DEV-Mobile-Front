package com.example.agend.diretor

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
    private val periodos = mutableListOf<PeriodoAulaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Botao voltar
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        // Carrega a tela onde o admin cadastra e gerencia os períodos de aula.
        setContentView(R.layout.activity_gerenciar_periodos_aula)

        listaPeriodos = findViewById(R.id.listaPeriodosAula)
        textoErro = findViewById(R.id.textoErroPeriodosAula)

        // Permite rolar a lista de períodos sem rolar a tela inteira.
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

        botaoGerarPeriodos = findViewById(R.id.botaoGerarPeriodosAula)

        // Limpa automaticamente os erros dos campos de geração automática.
        configurarLimpezaErro(editQuantidade, layoutQuantidade)
        configurarLimpezaErro(editHorarioInicialGeracao, layoutHorarioInicialGeracao)
        configurarLimpezaErro(editDuracaoAula, layoutDuracaoAula)
        configurarLimpezaErro(editIntervaloAposAula, layoutIntervaloAposAula)
        configurarLimpezaErro(editDuracaoIntervalo, layoutDuracaoIntervalo)

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

        botaoGerarPeriodos.setOnClickListener {
            gerarPeriodosAutomaticamente()
        }

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

    private fun carregarPeriodos() {
        mostrarErro(null)

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
        if (periodos.isEmpty()) {
            listaPeriodos.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                listOf("Nenhum período cadastrado.")
            )
            return
        }

        listaPeriodos.adapter = PeriodoAulaAdapter(
            context = this,
            periodos = periodos
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

        if (quantidade == null || quantidade <= 0) {
            layoutQuantidade.error = "Informe uma quantidade válida"
            return
        }

        if (!horarioInicial.matches(Regex("^\\d{2}:\\d{2}$"))) {
            layoutHorarioInicialGeracao.error = "Use o formato HH:mm"
            return
        }

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

        botaoGerarPeriodos.isEnabled = false
        botaoGerarPeriodos.text = "Gerando..."

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

        if (id.isNullOrBlank()) {
            mostrarErro("Período inválido.")
            return
        }

        val request = PeriodoAulaStatusRequest(
            ativo = !periodo.ativo
        )

        RetrofitClient.api.alterarStatusPeriodoAula(id, request)
            .enqueue(object : Callback<PeriodoAulaResponse> {

                override fun onResponse(
                    call: Call<PeriodoAulaResponse>,
                    response: Response<PeriodoAulaResponse>
                ) {
                    if (response.isSuccessful) {
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

            // Limpa o token em memória.
            RetrofitClient.token = null

            // Limpa a sessão salva no celular.
            SessionManager(this).limparSessao()

            // Volta para o login limpando a pilha de telas protegidas.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            mostrarErro(erro ?: "Erro no servidor: $codigo")
        }
    }
}