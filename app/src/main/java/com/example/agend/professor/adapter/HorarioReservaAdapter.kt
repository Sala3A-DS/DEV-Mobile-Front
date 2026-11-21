package com.example.agend.professor.adapter

import com.example.agend.utils.AcessibilidadeUtils
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.agend.R
import com.example.agend.auth.DisponibilidadeSalaResponse
import com.google.android.material.card.MaterialCardView

class HorarioReservaAdapter(
    private val context: Context,
    private val horarios: List<DisponibilidadeSalaResponse>
) : BaseAdapter() {

    override fun getCount(): Int {
        return horarios.size
    }

    override fun getItem(position: Int): DisponibilidadeSalaResponse {
        return horarios[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        // Reutiliza a view existente ou cria um novo card de horário.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_horario_reserva, parent, false)

        val cardHorario = view.findViewById<MaterialCardView>(R.id.cardHorario)
        val textoPeriodo = view.findViewById<TextView>(R.id.textoPeriodoHorario)
        val textoHora = view.findViewById<TextView>(R.id.textoHoraHorario)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusHorario)

        val horario = horarios[position]

        // Preenche o período e o intervalo de horário.
        textoPeriodo.text = "${horario.periodoAula}ª aula"
        textoHora.text = "${horario.horarioInicio} às ${horario.horarioFim}"

        if (horario.disponivel) {
            // Destaca o horário como disponível para reserva.
            textoStatus.text = "Disponível para reserva"
            textoStatus.setTextColor(
                context.getColor(R.color.yarooms_gold_light)
            )
            cardHorario.alpha = 1.0f
            cardHorario.strokeColor = context.getColor(R.color.yarooms_gold)
        } else {
            // Indica que o horário já está ocupado.
            textoStatus.text =
                "Ocupado por ${horario.professorNome ?: "outro professor"}"

            textoStatus.setTextColor(
                context.getColor(R.color.yarooms_error_light)
            )
            cardHorario.alpha = 1.0f
            cardHorario.alpha = 0.65f
            cardHorario.strokeColor =
                context.getColor(android.R.color.darker_gray)
        }

        // Une período, horário e situação em uma única leitura do TalkBack.
        val situacao = if (horario.disponivel) {
            "disponível para reserva"
        } else {
            "ocupado por ${horario.professorNome ?: "outro professor"}"
        }

        view.contentDescription =
            "${horario.periodoAula}ª aula, " +
                    "das ${AcessibilidadeUtils.falarHorario(horario.horarioInicio)} " +
                    "às ${AcessibilidadeUtils.falarHorario(horario.horarioFim)}, $situacao"

        // Evita a leitura duplicada dos textos internos do card.
        listOf(textoPeriodo, textoHora, textoStatus).forEach {
            it.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        return view
    }
}