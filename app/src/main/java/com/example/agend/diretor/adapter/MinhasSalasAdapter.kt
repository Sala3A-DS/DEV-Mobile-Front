package com.example.agend.diretor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.agend.R
import com.example.agend.auth.SalaResponse

class MinhasSalasAdapter(
    private val context: Context,
    private val salas: List<SalaResponse>
) : BaseAdapter() {

    override fun getCount(): Int {
        return salas.size
    }

    override fun getItem(position: Int): SalaResponse {
        return salas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_minha_sala, parent, false)

        val textoNomeEspaco = view.findViewById<TextView>(R.id.textoNomeEspaco)
        val textoLocalizacaoSala = view.findViewById<TextView>(R.id.textoLocalizacaoSala)
        val textoNumeroSala = view.findViewById<TextView>(R.id.textoNumeroSala)
        val textoStatusSala = view.findViewById<TextView>(R.id.textoStatusSala)

        val sala = salas[position]

        textoNomeEspaco.text = sala.nomeEspaco
        textoLocalizacaoSala.text = "Localização: ${sala.localizacao}"
        textoNumeroSala.text = "Número da sala: ${sala.numeroSala}"

        textoStatusSala.text = if (sala.ativa) {
            "Status: Ativa"
        } else {
            "Status: Inativa"
        }

        return view
    }
}