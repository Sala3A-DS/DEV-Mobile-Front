package com.example.agend.professor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.agend.R
import com.example.agend.auth.ReservaResponse
import com.google.android.material.button.MaterialButton

class MinhasReservasAdapter(
    private val context: Context,
    private val reservas: List<ReservaResponse>,
    private val onCancelarClick: (ReservaResponse) -> Unit
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
            .inflate(R.layout.item_minha_reserva, parent, false)

        val textoSala = view.findViewById<TextView>(R.id.textoSalaReserva)
        val textoTurma = view.findViewById<TextView>(R.id.textoTurmaReserva)
        val textoData = view.findViewById<TextView>(R.id.textoDataReserva)
        val textoHorario = view.findViewById<TextView>(R.id.textoHorarioReserva)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusReserva)
        val botaoCancelar = view.findViewById<MaterialButton>(R.id.botaoCancelarReserva)

        val reserva = reservas[position]

        val turma = if (reserva.turma.isBlank()) {
            "Não informada"
        } else {
            reserva.turma
        }

        textoSala.text = reserva.salaNome
        textoTurma.text = "Turma: $turma"
        textoData.text = "Data: ${reserva.data}"
        textoHorario.text =
            "Horário: ${reserva.periodoAula}ª aula - ${reserva.horarioInicio} às ${reserva.horarioFim}"
        textoStatus.text = "Status: ${reserva.status}"

        botaoCancelar.setOnClickListener {
            onCancelarClick(reserva)
        }

        return view
    }
}