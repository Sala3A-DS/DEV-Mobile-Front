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
        // Reutiliza a view existente ou infla o layout do item.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_opcao_uso, parent, false)

        val textoNome = view.findViewById<TextView>(R.id.textoNomeOpcaoUso)
        val textoStatus = view.findViewById<TextView>(R.id.textoStatusOpcaoUso)
        val botaoAlterarStatus = view.findViewById<Button>(R.id.botaoAlterarStatusOpcaoUso)

        val opcao = opcoes[position]

        // Exibe o nome da opção de uso.
        textoNome.text = opcao.nome

        // Atualiza o status e a ação disponível para a opção.
        if (opcao.ativa) {
            textoStatus.text = "Status: ativa"
            botaoAlterarStatus.text = "Desativar"
        } else {
            textoStatus.text = "Status: desativada"
            botaoAlterarStatus.text = "Ativar"
        }

        // Encaminha a alteração de status para a Activity.
        botaoAlterarStatus.setOnClickListener {
            onAlterarStatus(opcao)
        }

        return view
    }
}