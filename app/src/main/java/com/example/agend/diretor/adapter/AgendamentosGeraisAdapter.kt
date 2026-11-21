package com.example.agend.diretor.adapter

import com.example.agend.utils.AcessibilidadeUtils
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
        // Reutiliza a view existente ou infla o layout do item.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_agendamento_geral, parent, false)

        val textoSala = view.findViewById<TextView>(R.id.textoSalaAgendamentoGeral)
        val textoProfessor = view.findViewById<TextView>(R.id.textoProfessorAgendamentoGeral)
        val textoTurma = view.findViewById<TextView>(R.id.textoTurmaAgendamentoGeral)
        val textoFinalidade = view.findViewById<TextView>(R.id.textoFinalidadeAgendamentoGeral)
        val textoData = view.findViewById<TextView>(R.id.textoDataAgendamentoGeral)
        val textoHorario = view.findViewById<TextView>(R.id.textoHorarioAgendamentoGeral)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusAgendamentoGeral)

        val reserva = reservas[position]

        // Define valores padrão para campos opcionais da reserva.
        val turma = if (reserva.turma.isNullOrBlank()) {
            "Não informada"
        } else {
            reserva.turma
        }

        val finalidade = if (reserva.opcaoUsoNome.isNullOrBlank()) {
            "Não informada"
        } else {
            reserva.opcaoUsoNome
        }

        // Preenche os dados da reserva no layout.
        textoSala.text = reserva.salaNome
        textoProfessor.text = "Professor: ${reserva.professorNome}"
        textoTurma.text = "Turma: $turma"
        textoFinalidade.text = "Finalidade: $finalidade"
        textoData.text = "Data: ${reserva.data}"
        textoHorario.text =
            "Horário: ${reserva.periodoAula}ª aula - ${reserva.horarioInicio} às ${reserva.horarioFim}"
        textoStatus.text = "Status: ${reserva.status}"

        // Une todos os dados do agendamento em uma única leitura do TalkBack.
        textoSala.contentDescription =
            "${reserva.salaNome}. Professor ${reserva.professorNome}. " +
                    "Turma $turma. Finalidade $finalidade. " +
                    "${AcessibilidadeUtils.falarData(reserva.data)}, " +
                    "${reserva.periodoAula}ª aula, " +
                    "das ${AcessibilidadeUtils.falarHorario(reserva.horarioInicio)} " +
                    "às ${AcessibilidadeUtils.falarHorario(reserva.horarioFim)}. " +
                    "Status ${reserva.status}".lowercase()

        // Evita a leitura duplicada dos textos internos do card.
        listOf(
            textoProfessor,
            textoTurma,
            textoFinalidade,
            textoData,
            textoHorario,
            textoStatus
        ).forEach {
            it.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        return view
    }
}