package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.BookingResponse // Vamos criar isso abaixo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    // Lista visual e o adaptador
    private lateinit var adapter: ArrayAdapter<String>
    private val agendamentosFormatados = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val textoBemVindo = findViewById<TextView>(R.id.textoBemVindo)
        val textoNome = findViewById<TextView>(R.id.textoNomeUsuario)
        val lista = findViewById<ListView>(R.id.listaHomeAgendamentos)
        val botaoNovo = findViewById<Button>(R.id.botaoNovoAgendamento)

        // 1. Pegar os dados reais que vieram da tela de Login (Back-end)
        val email = intent.getStringExtra("email") ?: ""
        val nome = intent.getStringExtra("nome") ?: "Usuário"

        textoBemVindo.text = "Bem-vindo, $nome!"
        textoNome.text = "Professor(a)" // Você pode passar o cargo via Intent também, se quiser

        // 2. Configurar o visual da lista vazia
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, agendamentosFormatados)
        lista.adapter = adapter

        // 3. Botão de Novo Agendamento
        botaoNovo.setOnClickListener {
            val intent = Intent(this, AgendamentoActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("nome", nome)
            startActivity(intent) // Não precisamos mais do startActivityForResult!
        }
    }

    // O onResume é mágico: ele roda toda vez que a tela aparece para o usuário.
    // Ou seja, quando ele voltar da tela de "Novo Agendamento", a lista se atualiza sozinha!
    override fun onResume() {
        super.onResume()
        carregarAgendamentosDoServidor()
    }

    private fun carregarAgendamentosDoServidor() {
        // Pede os dados para o Back-end
        RetrofitClient.api.listarAgendamentos().enqueue(object : Callback<List<BookingResponse>> {

            override fun onResponse(call: Call<List<BookingResponse>>, response: Response<List<BookingResponse>>) {
                if (response.isSuccessful) {
                    val agendamentos = response.body() ?: emptyList()

                    // Limpa a lista antiga e preenche com os novos dados
                    agendamentosFormatados.clear()
                    for (item in agendamentos) {
                        // Monta o texto que vai aparecer em cada linha da lista
                        val linha = "Sala ID: ${item.spaceId}\nPor: ${item.nomeFuncionario}\nHora: ${item.dataHora}"
                        agendamentosFormatados.add(linha)
                    }

                    // Avisa a tela que os dados mudaram
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@HomeActivity, "Erro ao carregar lista", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookingResponse>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}