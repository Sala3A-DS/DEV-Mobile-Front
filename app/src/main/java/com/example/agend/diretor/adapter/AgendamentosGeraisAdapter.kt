package com.example.agend.diretor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.agend.R
import com.example.agend.auth.ReservaResponse

class AgendamentosGeraisAdapter(
    private val context: Context,
    private val reservas: List<ReservaResponse>
) : BaseAdapter() {

    override fun getCount(): Int {
        return reservas.size
    }

    override fun getItem(position: Int): ReservaResponse {
        return reservas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_agendamento_geral, parent, false)

        val textoSala = view.findViewById<TextView>(R.id.textoSalaAgendamentoGeral)
        val textoProfessor = view.findViewById<TextView>(R.id.textoProfessorAgendamentoGeral)
        val textoTurma = view.findViewById<TextView>(R.id.textoTurmaAgendamentoGeral)
        val textoData = view.findViewById<TextView>(R.id.textoDataAgendamentoGeral)
        val textoHorario = view.findViewById<TextView>(R.id.textoHorarioAgendamentoGeral)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusAgendamentoGeral)

        val reserva = reservas[position]

        val turma = if (reserva.turma.isBlank()) {
            "Não informada"
        } else {
            reserva.turma
        }

        textoSala.text = reserva.salaNome
        textoProfessor.text = "Professor: ${reserva.professorNome}"
        textoTurma.text = "Turma: $turma"
        textoData.text = "Data: ${reserva.data}"
        textoHorario.text =
            "Horário: ${reserva.periodoAula}ª aula - ${reserva.horarioInicio} às ${reserva.horarioFim}"
        textoStatus.text = "Status: ${reserva.status}"

        return view
    }
}