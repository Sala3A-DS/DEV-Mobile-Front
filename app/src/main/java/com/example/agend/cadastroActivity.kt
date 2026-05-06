package com.example.agend

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.agend.auth.RegisterRequest
import com.example.agend.auth.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        val editNome = findViewById<EditText>(R.id.editNome)
        val editEmail = findViewById<EditText>(R.id.editEmailCadastro)
        val editSenha = findViewById<EditText>(R.id.editSenhaCadastro)
        val botaoCadastrar = findViewById<Button>(R.id.botaoCadastrar)
        val textoVoltarLogin = findViewById<TextView>(R.id.textoVoltarLogin)
        val radioGrupo = findViewById<RadioGroup>(R.id.radioGrupoPerfil)

        botaoCadastrar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()
            val perfilId = radioGrupo.checkedRadioButtonId

            // 1. Validação local
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "⚠️ Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (perfilId == -1) {
                Toast.makeText(this, "⚠️ Selecione seu perfil!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Transforma a escolha do RadioButton no formato que o Back-end espera
            val cargo = when (perfilId) {
                R.id.radioProfessor -> "PROFESSOR"
                R.id.radioDiretor -> "ADM"
                else -> ""
            }

            // UI: Trava o botão para o usuário não enviar 2 cadastros iguais sem querer
            botaoCadastrar.isEnabled = false
            botaoCadastrar.text = "Cadastrando..."

            // 3. Monta o JSON que vai pro IntelliJ/Nuvem
            val pedido = RegisterRequest(nome, email, senha, cargo)

            // 4. Faz a requisição para o servidor
            RetrofitClient.api.register(pedido).enqueue(object : Callback<String> {

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"

                    // response.isSuccessful significa que o back-end devolveu um status 200 (OK)
                    if (response.isSuccessful) {
                        Toast.makeText(this@CadastroActivity, "✅ Cadastrado com sucesso!", Toast.LENGTH_LONG).show()

                        // Joga o usuário de volta pra tela de Login
                        startActivity(Intent(this@CadastroActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Se caiu aqui, é porque o e-mail já existe lá no Back-end (ele respondeu ERRO)
                        Toast.makeText(this@CadastroActivity, "⚠️ Erro: Este email já está em uso.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    botaoCadastrar.isEnabled = true
                    botaoCadastrar.text = "Cadastrar"
                    Toast.makeText(this@CadastroActivity, "⚠️ Falha na conexão com o servidor.", Toast.LENGTH_LONG).show()
                }
            })
        }

        textoVoltarLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}