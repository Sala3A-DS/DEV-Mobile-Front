package com.example.agend.diretor

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
import com.example.agend.auth.OpcaoUsoRequest
import com.example.agend.auth.OpcaoUsoResponse
import com.example.agend.auth.OpcaoUsoStatusRequest
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.adapter.OpcaoUsoAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GerenciarOpcoesUsoActivity : AppCompatActivity() {

    private lateinit var layoutNome: TextInputLayout
    private lateinit var editNome: TextInputEditText
    private lateinit var botaoCadastrar: Button
    private lateinit var listaOpcoes: ListView
    private lateinit var textoErro: TextView

    private val opcoes = mutableListOf<OpcaoUsoResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de gerenciamento de opções de uso.
        setContentView(R.layout.activity_gerenciar_opcoes_uso)

        // Configura o botão de retorno para fechar a tela atual.
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        layoutNome = findViewById(R.id.layoutNomeOpcaoUso)
        editNome = findViewById(R.id.editNomeOpcaoUso)
        botaoCadastrar = findViewById(R.id.botaoCadastrarOpcaoUso)
        listaOpcoes = findViewById(R.id.listaOpcoesUso)
        textoErro = findViewById(R.id.textoErroOpcoesUso)

        // Inicia o cadastro da opção informada pelo administrador.
        botaoCadastrar.setOnClickListener {
            cadastrarOpcao()
        }

        // Permite rolar a lista sem que o ScrollView externo intercepte o toque.
        listaOpcoes.setOnTouchListener { view, _ ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        // Carrega as opções cadastradas no backend.
        carregarOpcoes()
    }

    private fun cadastrarOpcao() {
        mostrarErro(null)
        layoutNome.error = null

        val nome = editNome.text.toString().trim()

        // Valida o preenchimento do nome da opção.
        if (nome.isBlank()) {
            layoutNome.error = "Informe o nome da opção"
            return
        }

        // Garante o tamanho mínimo do nome informado.
        if (nome.length < 3) {
            layoutNome.error = "Digite pelo menos 3 caracteres"
            return
        }

        // Evita múltiplos cadastros enquanto a requisição está em andamento.
        botaoCadastrar.isEnabled = false
        botaoCadastrar.text = "Cadastrando..."

        val request = OpcaoUsoRequest(
            nome = nome
        )

        // Envia a nova opção para o backend.
        RetrofitClient.api.cadastrarOpcaoUso(request)
            .enqueue(object : Callback<OpcaoUsoResponse> {

                override fun onResponse(
                    call: Call<OpcaoUsoResponse>,
                    response: Response<OpcaoUsoResponse>
                ) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar opção"

                    if (response.isSuccessful) {
                        editNome.setText("")

                        Toast.makeText(
                            this@GerenciarOpcoesUsoActivity,
                            "Opção cadastrada com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Atualiza a lista após o cadastro.
                        carregarOpcoes()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<OpcaoUsoResponse>, t: Throwable) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar opção"

                    mostrarErro("Falha na conexão ao cadastrar opção.")
                }
            })
    }

    private fun carregarOpcoes() {
        mostrarErro(null)

        // Consulta ao backend as opções disponíveis para o administrador.
        RetrofitClient.api.listarOpcoesUsoAdmin()
            .enqueue(object : Callback<List<OpcaoUsoResponse>> {

                override fun onResponse(
                    call: Call<List<OpcaoUsoResponse>>,
                    response: Response<List<OpcaoUsoResponse>>
                ) {
                    if (response.isSuccessful) {
                        val resposta = response.body() ?: emptyList()

                        opcoes.clear()
                        opcoes.addAll(resposta)

                        atualizarLista()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<List<OpcaoUsoResponse>>, t: Throwable) {
                    mostrarErro("Falha na conexão ao carregar opções.")
                }
            })
    }

    private fun atualizarLista() {
        if (opcoes.isEmpty()) {
            listaOpcoes.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                listOf("Nenhuma opção cadastrada.")
            )
            return
        }

        // Exibe as opções utilizando o adapter personalizado.
        listaOpcoes.adapter = OpcaoUsoAdapter(
            context = this,
            opcoes = opcoes
        ) { opcao ->
            alterarStatus(opcao)
        }
    }

    private fun alterarStatus(opcao: OpcaoUsoResponse) {
        val id = opcao.id

        // Impede a atualização quando a opção não possui um identificador válido.
        if (id.isNullOrBlank()) {
            mostrarErro("Opção inválida.")
            return
        }

        val novoStatus = !opcao.ativa

        val request = OpcaoUsoStatusRequest(
            ativa = novoStatus
        )

        // Atualiza o status da opção no backend.
        RetrofitClient.api.alterarStatusOpcaoUso(id, request)
            .enqueue(object : Callback<OpcaoUsoResponse> {

                override fun onResponse(
                    call: Call<OpcaoUsoResponse>,
                    response: Response<OpcaoUsoResponse>
                ) {
                    if (response.isSuccessful) {
                        // Recarrega a lista para refletir o novo status.
                        carregarOpcoes()
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<OpcaoUsoResponse>, t: Throwable) {
                    mostrarErro("Falha na conexão ao alterar status.")
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