package com.example.agend.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AppInfoDialog {

    fun mostrar(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Sobre o YaRooms")
            .setMessage(
                """
                Versão: 1.0.0
                
                O YaRooms é um aplicativo para agendamento de espaços escolares.
                
                Recursos principais:
                • Cadastro e login de usuários
                • Recuperação de senha por e-mail
                • Cadastro de espaços pelo diretor
                • Reserva de salas por professores
                • Consulta de disponibilidade
                • Cancelamento de reservas
                • Painel administrativo do diretor
                
                Plataforma atual:
                Android
                
                Observação:
                O APK está disponível apenas para dispositivos Android.
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }
}