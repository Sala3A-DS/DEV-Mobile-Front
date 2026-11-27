package com.example.agend

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.example.agend.gemini.GeminiKeyStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ConfiguracoesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_configuracoes)

        val chaves = GeminiKeyStore(applicationContext)
        val estado = findViewById<TextView>(R.id.estadoChaveGemini)
        val campo = findViewById<TextInputEditText>(R.id.entradaChaveGemini)
        val entrada = findViewById<TextInputLayout>(R.id.campoChaveGemini)
        val salvar = findViewById<MaterialButton>(R.id.salvarChaveGemini)
        val remover = findViewById<MaterialButton>(R.id.removerChaveGemini)

        fun atualizar() {
            val temChave = chaves.temChave()
            estado.setText(if (temChave) R.string.gemini_configured else R.string.gemini_default)
            entrada.hint = getString(if (temChave) R.string.gemini_replacement_key else R.string.gemini_api_key)
            remover.visibility = if (temChave) View.VISIBLE else View.GONE
        }

        fun editar(erro: Int, alteracao: suspend () -> Unit) = lifecycleScope.launch {
            salvar.isEnabled = false
            remover.isEnabled = false
            entrada.error = null
            try {
                alteracao()
                campo.text?.clear()
                getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(campo.windowToken, 0)
                campo.clearFocus()
                atualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                entrada.error = getString(erro)
            } finally {
                salvar.isEnabled = true
                remover.isEnabled = true
            }
        }

        ViewCompat.setAccessibilityHeading(findViewById(R.id.tituloConfiguracoes), true)
        ViewCompat.setAccessibilityHeading(findViewById(R.id.tituloGemini), true)
        findViewById<MaterialButton>(R.id.voltarConfiguracoes).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.abrirAiStudio).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")))
            } catch (_: ActivityNotFoundException) {
                entrada.error = getString(R.string.gemini_browser_error)
            }
        }
        salvar.setOnClickListener {
            val chave = campo.text?.toString()?.trim().orEmpty()
            if (chave.isEmpty() || chave.any { it <= ' ' || it > '~' }) {
                entrada.error = getString(R.string.gemini_key_format_error)
            } else {
                editar(R.string.gemini_store_error) { chaves.salvar(chave) }
            }
        }
        remover.setOnClickListener {
            editar(R.string.gemini_delete_error) { chaves.remover() }
        }
        atualizar()
    }
}
