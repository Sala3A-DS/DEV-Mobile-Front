package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.BookingResponse
import com.example.agend.auth.RetrofitClient // Certifique-se de que este import está correto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: ArrayAdapter<String>
    private val agendamentosFormatados = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val textoBemVindo = findViewById<TextView>(R.id.textoBemVindo)
        val textoNome = findViewById<TextView>(R.id.textoNomeUsuario)
        val lista = findViewById<ListView>(R.id.listaHomeAgendamentos)
        val botaoNovo = findViewById<Button>(R.id.botaoNovoAgendamento)

        val email = intent.getStringExtra("email") ?: ""
        val nome = intent.getStringExtra("nome") ?: "Usuário"

        textoBemVindo.text = "Bem-vindo, $nome!"
        textoNome.text = "Professor(a)"

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, agendamentosFormatados)
        lista.adapter = adapter

        botaoNovo.setOnClickListener {
            val intent = Intent(this, AgendamentoActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("nome", nome)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarAgendamentosDoServidor()
    }

    private fun carregarAgendamentosDoServidor() {
        // Pede os dados para o Back-end (O RetrofitClient enviará o Token automaticamente)
        RetrofitClient.api.listarAgendamentos().enqueue(object : Callback<List<BookingResponse>> {

            override fun onResponse(call: Call<List<BookingResponse>>, response: Response<List<BookingResponse>>) {
                if (response.isSuccessful) {
                    val agendamentos = response.body() ?: emptyList()

                    agendamentosFormatados.clear()
                    for (item in agendamentos) {
                        val linha = "Sala ID: ${item.spaceId}\nPor: ${item.nomeFuncionario}\nHora: ${item.dataHora}"
                        agendamentosFormatados.add(linha)
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    // ATUALIZADO: Tratamento de expiração de sessão (Token inválido ou expirado)
                    if (response.code() == 401 || response.code() == 403) {
                        Toast.makeText(this@HomeActivity, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()

                        // Limpa o token salvo para evitar problemas
                        RetrofitClient.token = null

                        // Manda de volta para o Login e fecha a tela atual
                        val intent = Intent(this@HomeActivity, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity() // Fecha todas as telas abertas para o usuário não conseguir voltar clicando em "Voltar"
                    } else {
                        Toast.makeText(this@HomeActivity, "Erro ao carregar lista: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<BookingResponse>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}