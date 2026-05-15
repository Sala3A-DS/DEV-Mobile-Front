package com.example.agend.professor.adapter

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_horario_reserva, parent, false)

        val cardHorario = view.findViewById<MaterialCardView>(R.id.cardHorario)
        val textoPeriodo = view.findViewById<TextView>(R.id.textoPeriodoHorario)
        val textoHora = view.findViewById<TextView>(R.id.textoHoraHorario)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusHorario)

        val horario = horarios[position]

        textoPeriodo.text = "${horario.periodoAula}ª aula"
        textoHora.text = "${horario.horarioInicio} às ${horario.horarioFim}"

        if (horario.disponivel) {
            textoStatus.text = "Disponível para reserva"
            textoStatus.setTextColor(context.getColor(R.color.yarooms_gold))
            cardHorario.alpha = 1.0f
            cardHorario.strokeColor = context.getColor(R.color.yarooms_gold)
        } else {
            textoStatus.text = "Ocupado por ${horario.professorNome ?: "outro professor"}"
            textoStatus.setTextColor(context.getColor(android.R.color.holo_red_light))
            cardHorario.alpha = 0.65f
            cardHorario.strokeColor = context.getColor(android.R.color.darker_gray)
        }

        return view
    }
}