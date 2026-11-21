package com.example.agend.utils

import java.text.SimpleDateFormat
import java.util.Locale
import android.view.View
import android.widget.Button
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

object AcessibilidadeUtils {

    // Faz o TalkBack anunciar a view como botão.
    fun marcarComoBotao(view: View) {
        ViewCompat.setAccessibilityDelegate(view, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = Button::class.java.name
            }
        })
    }

    // Converte "07:50" em "7 horas e 50 minutos" para leitura falada.
    fun falarHorario(hora: String?): String {
        if (hora.isNullOrBlank()) return ""

        val partes = hora.split(":")
        val h = partes.getOrNull(0)?.toIntOrNull() ?: return hora
        val m = partes.getOrNull(1)?.toIntOrNull() ?: 0

        val horas = if (h == 1) "1 hora" else "$h horas"
        return if (m == 0) horas else "$horas e $m minutos"
    }

    // Converte "2026-05-20" em "20 de maio de 2026" para leitura falada.
    fun falarData(dataIso: String?): String {
        if (dataIso.isNullOrBlank()) return ""

        return try {
            val data = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR")).parse(dataIso)
            SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("pt", "BR")).format(data!!)
        } catch (e: Exception) {
            dataIso
        }
    }
}