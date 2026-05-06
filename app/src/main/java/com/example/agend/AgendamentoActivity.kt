package com.example.agend

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.BookingRequest
import com.example.agend.auth.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AgendamentoActivity : AppCompatActivity() {

    // Variáveis para guardar as escolhas do usuário em formato de número
    // Isso vai facilitar muito na hora de enviar para o Spring Boot!
    private var selAno = 0
    private var selMes = 0
    private var selDia = 0
    private var selHora = -1
    private var selMinuto = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendamento)

        val editSala = findViewById<EditText>(R.id.editSala)
        val botaoData = findViewById<Button>(R.id.botaoData)
        val textoData = findViewById<TextView>(R.id.textoDataSelecionada)
        val botaoHorario = findViewById<Button>(R.id.botaoHorario)
        val textoHorario = findViewById<TextView>(R.id.textoHorarioSelecionado)
        val botaoAgendar = findViewById<Button>(R.id.botaoAgendar)

        // Pega o nome do professor que veio da tela Home
        val nomeFuncionario = intent.getStringExtra("nome") ?: "Professor Desconhecido"

        botaoData.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    selAno = ano
                    selMes = mes + 1 // O Android conta os meses do 0 ao 11, o Back-end do 1 ao 12
                    selDia = dia
                    textoData.text = "📅 Data: ${String.format("%02d/%02d/%04d", dia, selMes, ano)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        botaoHorario.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hora, minuto ->
                    selHora = hora
                    selMinuto = minuto
                    textoHorario.text = "⏰ Horário: ${String.format("%02d:%02d", hora, minuto)}"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        botaoAgendar.setOnClickListener {
            val salaDigitada = editSala.text.toString().trim()

            // 1. Validações Locais
            if (salaDigitada.isEmpty()) {
                Toast.makeText(this, "⚠️ Digite o número ou ID da sala!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selAno == 0) {
                Toast.makeText(this, "⚠️ Selecione uma data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selHora == -1) {
                Toast.makeText(this, "⚠️ Selecione um horário!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // O Back-end espera um número inteiro para o ID do espaço.
            // Se o usuário digitar "Sala 1", vamos tentar pegar só o número. Se não conseguir, mandamos 1 por padrão.
            val spaceId = salaDigitada.toIntOrNull() ?: 1

            // 2. Formata a DataHora para o padrão do Spring Boot (Ex: "2026-10-15T14:30:00")
            val dataHoraFormatada = String.format("%04d-%02d-%02dT%02d:%02d:00", selAno, selMes, selDia, selHora, selMinuto)

            // UI: Trava o botão
            botaoAgendar.isEnabled = false
            botaoAgendar.text = "Agendando..."

            // 3. Monta o JSON para o Back-end
            val pedido = BookingRequest(
                nomeFuncionario = nomeFuncionario,
                spaceId = spaceId,
                dataHora = dataHoraFormatada
            )

            // 4. Envia para o IntelliJ/Nuvem
            RetrofitClient.api.makeBooking(pedido).enqueue(object : Callback<String> {

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoAgendar.isEnabled = true
                    botaoAgendar.text = "Confirmar Agendamento"

                    if (response.isSuccessful && response.body()?.contains("SUCESSO") == true) {
                        Toast.makeText(this@AgendamentoActivity, "✅ Agendamento realizado!", Toast.LENGTH_LONG).show()
                        finish() // Volta para a Home, que agora já vai atualizar a lista sozinha!
                    } else {
                        // Se o horário estiver ocupado, o Spring Boot avisa!
                        Toast.makeText(this@AgendamentoActivity, "⚠️ Erro: Horário já ocupado.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoAgendar.isEnabled = true
                    botaoAgendar.text = "Confirmar Agendamento"
                    Toast.makeText(this@AgendamentoActivity, "⚠️ Erro de conexão.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}