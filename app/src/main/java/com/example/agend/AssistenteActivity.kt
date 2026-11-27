package com.example.agend

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agend.auth.SessionManager
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AssistenteActivity : AppCompatActivity() {
    private val guia = """
        Você é o assistente de navegação do YaRooms. Responda em português com passos curtos.
        Professor e coordenador: em Reservar sala, selecione espaço, data, turno, turma e
        finalidade de uso. Atualize a disponibilidade e escolha um horário disponível.
        Em Minhas reservas, consulte suas reservas, atualize a lista ou confirme o cancelamento.
        Sem conexão, essa tela mostra a última consulta salva no aparelho, se disponível.
        Diretor: cadastre salas, consulte Minhas salas e Agendamentos gerais, gerencie opções
        de uso e períodos de aula e altere o sábado letivo pelo painel da direção.
        Você apenas orienta. Não consegue ver dados de contas, consultar horários em tempo real
        ou executar ações. Se o guia não esclarecer a dúvida, diga isso. Nunca peça senhas.
    """.trimIndent()

    private val chat by lazy {
        val perfil = SessionManager(this).getCargo()
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-flash-latest",
            systemInstruction = content { text("$guia\nPerfil atual: $perfil") }
        ).startChat()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistente)
        val campo = findViewById<EditText>(R.id.perguntaChat)
        val enviar = findViewById<Button>(R.id.enviarChat)
        findViewById<Button>(R.id.voltarChat).setOnClickListener { finish() }
        mensagem("YaRooms", "Como posso ajudar você a usar o aplicativo?", false)
        enviar.setOnClickListener {
            val pergunta = campo.text.toString().trim()
            if (pergunta.isEmpty()) return@setOnClickListener
            mensagem("Você", pergunta, true)
            campo.setText("")
            enviar.isEnabled = false
            enviar.text = "Respondendo…"
            lifecycleScope.launch {
                try {
                    val resposta = chat.sendMessage(pergunta).text
                    mensagem("YaRooms", resposta ?: "Tente reformular sua pergunta.", false)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    mensagem("YaRooms", "O assistente está indisponível. Tente enviar novamente.", false)
                    if (campo.text.isEmpty()) campo.setText(pergunta)
                } finally {
                    enviar.isEnabled = true
                    enviar.text = "Enviar"
                }
            }
        }
    }
    private fun mensagem(autor: String, texto: String, propria: Boolean) {
        val lista = findViewById<LinearLayout>(R.id.mensagens)
        val escala = resources.displayMetrics.density
        val margem = (12 * escala).toInt()
        val bloco = TextView(this).apply {
            text = "$autor\n$texto"
            textSize = 17f
            setTextColor(getColor(if (propria) R.color.yarooms_blue_dark else R.color.white))
            background = GradientDrawable().apply {
                cornerRadius = 18 * escala
                setColor(getColor(if (propria) R.color.yarooms_gold_light else R.color.yarooms_blue_light))
            }
            setPadding(margem, margem, margem, margem)
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        lista.addView(bloco, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(if (propria) margem * 2 else 0, margem, if (propria) 0 else margem * 2, margem / 2)
        })
        findViewById<ScrollView>(R.id.rolagemChat).post {
            findViewById<ScrollView>(R.id.rolagemChat).fullScroll(View.FOCUS_DOWN)
        }
    }
}
