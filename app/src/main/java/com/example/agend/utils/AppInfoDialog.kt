package com.example.agend.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object AppInfoDialog {

    // Exibe as informações e os recursos disponíveis no aplicativo.
    fun mostrar(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Sobre o YaRooms")
            .setMessage(
                """
                Versão: 5.0.2
                
                O YaRooms é um aplicativo para agendamento de espaços escolares.
                
                Recursos principais:
                - Cadastro e login de usuários
                - Recuperação de senha por e-mail
                - Cadastro de espaços pelo diretor
                - Reserva de salas por professores
                - Consulta de disponibilidade
                - Cancelamento de reservas
                - Painel administrativo do diretor
                
                Desenvolvido por:
                 - Documentção e Requsisitos
                Kevin Vieira
                Miguel Lima
                Ana Clara
                Marcella de Oliveira
                Maicon Douglas
                Maria Eduarda Izzo

                - Prototipagem
                João Victor
                Maria Eduarda Alves
                Filipe de Jesus
                Gabriel de Sousa
                Dhyan Gabriel

                - Desenvolvimento
                Icaro de Oliveira (Mobile)
                Giovany Veloso (Mobile)
                Gustavo Henrique (Web)
                Leonardo Oliveira (Web) 
                Brian José (Web)
                Henry Gabriel (Mobile)
                Bryan Rodrigues (Mobile)

                - Eventos
                Kauã Lima
                Matheus Gonçalves
                Emerson Gabriel
                Eduardo Barreto
                Lara Rodrigues
                Miguel Cardoso
                Douglas do Carmo
                
                Plataforma atual:
                Android
                
                Observação:
                O APK está disponível apenas para dispositivos Android.
                """.trimIndent()
            )
            // Fecha o diálogo ao confirmar a leitura.
            .setPositiveButton("OK", null)
            .show()
    }
}