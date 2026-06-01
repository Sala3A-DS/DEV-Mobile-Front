package com.example.agend.diretor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.agend.R
import com.example.agend.auth.PeriodoAulaResponse

class PeriodoAulaAdapter(
    private val context: Context,
    private val periodos: List<PeriodoAulaResponse>,
    private val onAlterarStatus: (PeriodoAulaResponse) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int {
        return periodos.size
    }

    override fun getItem(position: Int): PeriodoAulaResponse {
        return periodos[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_periodo_aula, parent, false)

        val textoNumero = view.findViewById<TextView>(R.id.textoNumeroPeriodoAula)
        val textoHorario = view.findViewById<TextView>(R.id.textoHorarioPeriodoAula)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusPeriodoAula)
        val botaoAlterarStatus = view.findViewById<Button>(R.id.botaoAlterarStatusPeriodoAula)

        val periodo = periodos[position]

        textoNumero.text = "${periodo.numero}ª aula"
        textoHorario.text = "${periodo.horarioInicio} às ${periodo.horarioFim}"

        if (periodo.ativo) {
            textoStatus.text = "Status: ativo"
            botaoAlterarStatus.text = "Desativar"
        } else {
            textoStatus.text = "Status: desativado"
            botaoAlterarStatus.text = "Ativar"
        }

        // Envia o período selecionado para a Activity alterar o status.
        botaoAlterarStatus.setOnClickListener {
            onAlterarStatus(periodo)
        }

        return view
    }
}