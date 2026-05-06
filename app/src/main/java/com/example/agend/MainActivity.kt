package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.EsqueciSenhaActivity
import com.example.agend.HomeActivity
import com.example.agend.auth.LoginRequest
import com.example.agend.auth.RetrofitClient
import com.example.agend.auth.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editSenha = findViewById<EditText>(R.id.editSenha)
        val botaoEntrar = findViewById<Button>(R.id.botaoEntrar)
        val textoCadastro = findViewById<TextView>(R.id.textoCadastro)
        val textoErro = findViewById<TextView>(R.id.textoErroLogin)
        val textoEsqueci = findViewById<TextView>(R.id.textoEsqueciSenha)

        botaoEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()
            textoErro.visibility = View.GONE

            // 1. Validação simples para ver se o usuário não deixou em branco
            if (email.isEmpty() || senha.isEmpty()) {
                textoErro.text = "⚠️ Preencha o email e a senha!"
                textoErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Dica de UI: Muda o texto do botão enquanto carrega para o usuário não clicar 2 vezes
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Conectando..."

            // 2. Montamos o pedido para a API
            val pedido = LoginRequest(email, senha)

            // 3. Enviamos para o Back-end
            RetrofitClient.api.login(pedido).enqueue(object : Callback<UserResponse> {

                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    // Restaura o botão
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"

                    if (response.isSuccessful) {
                        val usuario = response.body()

                        // SUCESSO! O Back-end confirmou. Vamos para a Home.
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        intent.putExtra("email", usuario?.email)
                        intent.putExtra("nome", usuario?.nome) // Passando o nome real do banco de dados!
                        startActivity(intent)
                        finish()
                    } else {
                        // ERRO: O Back-end respondeu, mas disse que a senha está errada
                        textoErro.text = "⚠️ E-mail ou senha incorretos."
                        textoErro.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    // FALHA: O Servidor no IntelliJ está desligado ou o celular sem internet
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"

                    textoErro.text = "⚠️ Falha na conexão com o servidor."
                    textoErro.visibility = View.VISIBLE
                }
            })
        }

        textoCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        textoEsqueci.setOnClickListener {
            startActivity(Intent(this, EsqueciSenhaActivity::class.java))
        }
    }
}