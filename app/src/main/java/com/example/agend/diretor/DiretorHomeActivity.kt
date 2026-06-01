package com.example.agend.diretor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.MainActivity
import com.example.agend.R
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.SessionManager
import com.example.agend.utils.AppInfoDialog
import android.widget.Toast
import com.example.agend.auth.ConfiguracaoSistemaResponse
import com.example.agend.auth.SabadoLetivoRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiretorHomeActivity : AppCompatActivity() {

    private lateinit var botaoSabadoLetivo: Button
    private var sabadoLetivoAtivo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carrega a tela inicial do diretor/admin.
        setContentView(R.layout.activity_diretor_home)

        val textoNomeDiretor = findViewById<TextView>(R.id.textoNomeDiretor)
        val botaoCadastrarSala = findViewById<Button>(R.id.botaoCadastrarSala)
        val botaoMinhasSalas = findViewById<Button>(R.id.botaoMinhasSalas)
        val botaoAgendamentosGerais = findViewById<Button>(R.id.botaoAgendamentosGerais)
        val botaoOpcoesUso = findViewById<Button>(R.id.botaoOpcoesUso)
        val botaoPeriodosAula = findViewById<Button>(R.id.botaoPeriodosAula)
        botaoSabadoLetivo = findViewById(R.id.botaoSabadoLetivo)
        val botaoSairDiretor = findViewById<Button>(R.id.botaoSairDiretor)

        // Recebe dados enviados pela tela de login ou pela sessão salva.
        val nome = intent.getStringExtra("nome") ?: "Diretor"

        textoNomeDiretor.text = "Olá, $nome"

        // Abre a tela de cadastro de espaço/sala.
        botaoCadastrarSala.setOnClickListener {
            startActivity(Intent(this, CadastrarSalaActivity::class.java))
        }

        // Abre a tela com as salas cadastradas pelo diretor.
        botaoMinhasSalas.setOnClickListener {
            startActivity(Intent(this, MinhasSalasActivity::class.java))
        }

        // Abre a tela com todos os agendamentos feitos.
        botaoAgendamentosGerais.setOnClickListener {
            startActivity(Intent(this, AgendamentosGeraisActivity::class.java))
        }

        botaoOpcoesUso.setOnClickListener {
            startActivity(Intent(this, GerenciarOpcoesUsoActivity::class.java))
        }

        botaoPeriodosAula.setOnClickListener {
            startActivity(Intent(this, GerenciarPeriodosAulaActivity::class.java))
        }

        botaoSabadoLetivo.setOnClickListener {
            atualizarSabadoLetivo(!sabadoLetivoAtivo)
        }

        // Sai da conta, limpa o token em memória e remove a sessão salva no celular.
        botaoSairDiretor.setOnClickListener {
            RetrofitClient.token = null
            SessionManager(this).limparSessao()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Icone de Info
        val botaoInfoApp = findViewById<TextView>(R.id.botaoInfoApp)

        botaoInfoApp.setOnClickListener {
            AppInfoDialog.mostrar(this)
        }
        carregarConfiguracaoSistema()
    }
    private fun carregarConfiguracaoSistema() {
        botaoSabadoLetivo.isEnabled = false
        botaoSabadoLetivo.text = "Sábado letivo: carregando..."

        RetrofitClient.api.buscarConfiguracaoSistema()
            .enqueue(object : Callback<ConfiguracaoSistemaResponse> {

                override fun onResponse(
                    call: Call<ConfiguracaoSistemaResponse>,
                    response: Response<ConfiguracaoSistemaResponse>
                ) {
                    if (response.isSuccessful) {
                        val configuracao = response.body()

                        sabadoLetivoAtivo = configuracao?.sabadoLetivo ?: false

                        atualizarTextoBotaoSabado()
                    } else {
                        botaoSabadoLetivo.text = "Erro ao carregar sábado letivo"
                        Toast.makeText(
                            this@DiretorHomeActivity,
                            "Erro ao carregar configuração.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    botaoSabadoLetivo.isEnabled = true
                }

                override fun onFailure(call: Call<ConfiguracaoSistemaResponse>, t: Throwable) {
                    botaoSabadoLetivo.isEnabled = true
                    botaoSabadoLetivo.text = "Erro ao carregar sábado letivo"

                    Toast.makeText(
                        this@DiretorHomeActivity,
                        "Falha na conexão ao carregar configuração.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun atualizarSabadoLetivo(novoValor: Boolean) {
        botaoSabadoLetivo.isEnabled = false
        botaoSabadoLetivo.text = "Atualizando..."

        val request = SabadoLetivoRequest(
            sabadoLetivo = novoValor
        )

        RetrofitClient.api.atualizarSabadoLetivo(request)
            .enqueue(object : Callback<ConfiguracaoSistemaResponse> {

                override fun onResponse(
                    call: Call<ConfiguracaoSistemaResponse>,
                    response: Response<ConfiguracaoSistemaResponse>
                ) {
                    if (response.isSuccessful) {
                        val configuracao = response.body()

                        sabadoLetivoAtivo = configuracao?.sabadoLetivo ?: false

                        atualizarTextoBotaoSabado()

                        val mensagem = if (sabadoLetivoAtivo) {
                            "Sábado letivo ativado."
                        } else {
                            "Sábado letivo desativado."
                        }

                        Toast.makeText(
                            this@DiretorHomeActivity,
                            mensagem,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        atualizarTextoBotaoSabado()

                        Toast.makeText(
                            this@DiretorHomeActivity,
                            "Erro ao atualizar sábado letivo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    botaoSabadoLetivo.isEnabled = true
                }

                override fun onFailure(call: Call<ConfiguracaoSistemaResponse>, t: Throwable) {
                    botaoSabadoLetivo.isEnabled = true
                    atualizarTextoBotaoSabado()

                    Toast.makeText(
                        this@DiretorHomeActivity,
                        "Falha na conexão ao atualizar configuração.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun atualizarTextoBotaoSabado() {
        botaoSabadoLetivo.text = if (sabadoLetivoAtivo) {
            "Sábado letivo: ativado"
        } else {
            "Sábado letivo: desativado"
        }
    }
}