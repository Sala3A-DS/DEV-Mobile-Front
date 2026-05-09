package com.example.agend

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.BookingRequest
import com.example.agend.auth.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AgendamentoActivity : AppCompatActivity() {

    private var selAno    = 0
    private var selMes    = 0
    private var selDia    = 0
    private var selHora   = -1
    private var selMinuto = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agendamento)

        val layoutSala   = findViewById<TextInputLayout>(R.id.layoutSala)
        val editSala     = findViewById<TextInputEditText>(R.id.editSala)
        val botaoData    = findViewById<Button>(R.id.botaoData)
        val textoData    = findViewById<TextView>(R.id.textoDataSelecionada)
        val botaoHorario = findViewById<Button>(R.id.botaoHorario)
        val textoHorario = findViewById<TextView>(R.id.textoHorarioSelecionado)
        val botaoAgendar = findViewById<Button>(R.id.botaoAgendar)

        val nomeFuncionario = intent.getStringExtra("nome") ?: "Professor Desconhecido"

        botaoData.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    selAno = ano
                    selMes = mes + 1
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
                    selHora   = hora
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

            layoutSala.error = null

            if (salaDigitada.isEmpty()) {
                layoutSala.error = "Digite o nome ou ID da sala"
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

            val spaceId = salaDigitada.toIntOrNull() ?: 1
            val dataHoraFormatada = String.format(
                "%04d-%02d-%02dT%02d:%02d:00",
                selAno, selMes, selDia, selHora, selMinuto
            )

            botaoAgendar.isEnabled = false
            botaoAgendar.text = "Agendando..."

            val pedido = BookingRequest(
                nomeFuncionario = nomeFuncionario,
                spaceId         = spaceId,
                dataHora        = dataHoraFormatada
            )

            RetrofitClient.api.makeBooking(pedido).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoAgendar.isEnabled = true
                    botaoAgendar.text = "Confirmar Agendamento"

                    if (response.isSuccessful) {
                        if (response.body()?.contains("SUCESSO") == true) {
                            Toast.makeText(this@AgendamentoActivity, "✅ Agendamento realizado!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@AgendamentoActivity, "⚠️ Erro: Horário já ocupado.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // NOVO: Tratamento caso o Token seja inválido ou falte
                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(this@AgendamentoActivity, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()
                            // Opcional: Redirecionar para o Login se a sessão cair
                            // startActivity(Intent(this@AgendamentoActivity, LoginActivity::class.java))
                            // finishAffinity()
                        } else {
                            Toast.makeText(this@AgendamentoActivity, "⚠️ Erro no servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
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