package com.example.agend.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class NarradorAcessibilidade(
    context: Context,
    private val aoFicarIndisponivel: (() -> Unit)? = null
) {

    private var motor: TextToSpeech? = null
    private var pronto = false

    // Guarda a fala pedida antes de o motor terminar de iniciar.
    private var falaPendente: String? = null

    init {
        motor = TextToSpeech(context.applicationContext) { status ->
            aoIniciarMotor(status)
        }
    }

    private fun aoIniciarMotor(status: Int) {
        val tts = motor ?: return

        if (status != TextToSpeech.SUCCESS) {
            aoFicarIndisponivel?.invoke()
            return
        }

        val resultado = tts.setLanguage(Locale.forLanguageTag("pt-BR"))

        // Verifica se a voz em português do Brasil está instalada.
        if (resultado == TextToSpeech.LANG_MISSING_DATA ||
            resultado == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            aoFicarIndisponivel?.invoke()
            return
        }

        pronto = true

        // Fala o texto que foi solicitado durante a inicialização.
        falaPendente?.let { falar(it) }
        falaPendente = null
    }

    // Fala o texto, interrompendo qualquer narração em andamento.
    fun falar(texto: String) {
        if (texto.isBlank()) return

        if (!pronto) {
            falaPendente = texto
            return
        }

        motor?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "narracao_yarooms")
    }

    // Interrompe a narração em andamento.
    fun parar() {
        motor?.stop()
    }

    // Libera o motor de voz quando a tela é encerrada.
    fun liberar() {
        motor?.stop()
        motor?.shutdown()
        motor = null
        pronto = false
    }
}