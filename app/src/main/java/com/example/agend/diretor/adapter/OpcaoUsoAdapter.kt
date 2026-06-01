package com.example.agend.diretor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.agend.R
import com.example.agend.auth.OpcaoUsoResponse

class OpcaoUsoAdapter(
    private val context: Context,
    private val opcoes: List<OpcaoUsoResponse>,
    private val onAlterarStatus: (OpcaoUsoResponse) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int {
        return opcoes.size
    }

    override fun getItem(position: Int): OpcaoUsoResponse {
        return opcoes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_opcao_uso, parent, false)

        val textoNome = view.findViewById<TextView>(R.id.textoNomeOpcaoUso)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusOpcaoUso)
        val botaoAlterarStatus = view.findViewById<Button>(R.id.botaoAlterarStatusOpcaoUso)

        val opcao = opcoes[position]

        textoNome.text = opcao.nome

        if (opcao.ativa) {
            textoStatus.text = "Status: ativa"
            botaoAlterarStatus.text = "Desativar"
        } else {
            textoStatus.text = "Status: desativada"
            botaoAlterarStatus.text = "Ativar"
        }

        // Chama a função da Activity para ativar ou desativar a opção.
        botaoAlterarStatus.setOnClickListener {
            onAlterarStatus(opcao)
        }

        return view
    }
}