package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.ForgotPasswordRequest
import com.example.agend.auth.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EsqueciSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_esqueci_senha)

        val editEmail = findViewById<EditText>(R.id.editEmailEsqueci)

        // NOVO: Precisamos capturar a nova senha que o usuário quer colocar!
        // (Lembre-se de criar este EditText no seu activity_esqueci_senha.xml)
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenhaEsqueci)

        val botaoRedefinir = findViewById<Button>(R.id.botaoRedefinir)
        val textoErro = findViewById<TextView>(R.id.textoErroEsqueci)
        val textoSucesso = findViewById<TextView>(R.id.textoSucessoEsqueci)
        val textoVoltar = findViewById<TextView>(R.id.textoVoltarEsqueci)

        botaoRedefinir.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val novaSenha = editNovaSenha.text.toString().trim()

            textoErro.visibility = View.GONE
            textoSucesso.visibility = View.GONE

            // 1. Validação
            if (email.isEmpty() || novaSenha.isEmpty()) {
                textoErro.text = "⚠️ Preencha o email e a nova senha!"
                textoErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // UI: Feedback visual de carregamento
            botaoRedefinir.isEnabled = false
            botaoRedefinir.text = "Aguarde..."

            // 2. Monta o pacote de dados exigido pelo seu Back-end
            val pedido = ForgotPasswordRequest(email, novaSenha)

            // 3. Faz a requisição para a Nuvem/IntelliJ
            RetrofitClient.api.resetPassword(pedido).enqueue(object : Callback<String> {

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoRedefinir.isEnabled = true
                    botaoRedefinir.text = "Redefinir Senha"

                    if (response.isSuccessful) {
                        // O Spring Boot retornou "SUCESSO..."
                        textoSucesso.text = "✅ Senha alterada com sucesso!"
                        textoSucesso.visibility = View.VISIBLE

                        // Limpa os campos para não ficar visível
                        editEmail.text.clear()
                        editNovaSenha.text.clear()
                    } else {
                        // O Spring Boot retornou "ERRO: Não encontramos um usuário..." (Status 404)
                        textoErro.text = "⚠️ Email não cadastrado no sistema!"
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoRedefinir.isEnabled = true
                    botaoRedefinir.text = "Redefinir Senha"

                    textoErro.text = "⚠️ Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        textoVoltar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Destrói a tela atual para não acumular na memória do celular
        }
    }
}