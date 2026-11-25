package com.example.agend.diretor

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
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SalaResponse
import com.example.agend.auth.SessionManager
import com.example.agend.diretor.adapter.MinhasSalasAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MinhasSalasActivity : AppCompatActivity() {

    private lateinit var listaMinhasSalas: ListView
    private lateinit var textoErro: TextView
    private lateinit var botaoAtualizar: Button

    // Armazena as salas retornadas pelo backend.
    private val salas = mutableListOf<SalaResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa a tela de salas cadastradas pelo diretor.
        setContentView(R.layout.activity_minhas_salas)

        // Configura o botão de retorno para fechar a tela atual.
        val layoutVoltarTopo = findViewById<LinearLayout>(R.id.layoutVoltarTopo)

        layoutVoltarTopo.setOnClickListener {
            finish()
        }

        // Anuncia o retorno como botão para o TalkBack.
        AcessibilidadeUtils.marcarComoBotao(layoutVoltarTopo)

        // Define o título da tela para navegação por títulos no TalkBack.
        ViewCompat.setAccessibilityHeading(
            findViewById<TextView>(R.id.textoTituloMinhasSalas),
            true
        )

        listaMinhasSalas = findViewById(R.id.listaMinhasSalas)
        // Permite rolar a lista sem interferência do ScrollView pai.
        listaMinhasSalas.setOnTouchListener { view, _ ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        textoErro = findViewById(R.id.textoErroMinhasSalas)
        botaoAtualizar = findViewById(R.id.botaoAtualizarMinhasSalas)

        // Anuncia automaticamente as mensagens de erro da tela.
        ViewCompat.setAccessibilityLiveRegion(
            textoErro,
            ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE
        )

        // Permite atualizar manualmente a lista de salas.
        botaoAtualizar.setOnClickListener {
            carregarMinhasSalas()
        }

        // Carrega as salas ao abrir a tela.
        carregarMinhasSalas()
    }

    private fun carregarMinhasSalas() {
        mostrarErro(null)

        botaoAtualizar.isEnabled = false
        botaoAtualizar.text = "Carregando..."

        // Consulta ao backend as salas cadastradas pelo diretor.
        RetrofitClient.api.listarMinhasSalas()
            .enqueue(object : Callback<List<SalaResponse>> {

                override fun onResponse(
                    call: Call<List<SalaResponse>>,
                    response: Response<List<SalaResponse>>
                ) {
                    botaoAtualizar.isEnabled = true
                    botaoAtualizar.text = "Atualizar"

                    if (response.isSuccessful) {
                        val resposta = response.body() ?: emptyList()

                        salas.clear()
                        salas.addAll(resposta)

                        if (salas.isEmpty()) {
                            // Exibe uma mensagem quando não existem salas cadastradas.
                            listaMinhasSalas.adapter = ArrayAdapter(
                                this@MinhasSalasActivity,
                                R.layout.item_lista_vazia,
                                listOf("Você ainda não possui salas cadastradas.")
                            )
                        } else {
                            // Exibe as salas utilizando o adapter personalizado.
                            listaMinhasSalas.adapter = MinhasSalasAdapter(
                                context = this@MinhasSalasActivity,
                                salas = salas
                            )
                        }

                        // Informa o resultado da consulta para quem usa o TalkBack.
                        listaMinhasSalas.announceForAccessibility(
                            when (salas.size) {
                                0 -> "Você ainda não possui salas cadastradas"
                                1 -> "1 sala carregada"
                                else -> "${salas.size} salas carregadas"
                            }
                        )
                    } else {
                        tratarErroSessaoOuServidor(
                            response.code(),
                            response.errorBody()?.string()
                        )
                    }
                }

                override fun onFailure(call: Call<List<SalaResponse>>, t: Throwable) {
                    botaoAtualizar.isEnabled = true
                    botaoAtualizar.text = "Atualizar"

                    mostrarErro("Falha na conexão ao carregar salas.")
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