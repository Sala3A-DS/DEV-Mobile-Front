package com.example.agend.auth

import android.content.Context

class SessionManager(context: Context) {

    // Armazena os dados da sessão localmente usando SharedPreferences.
    private val prefs = context.getSharedPreferences("yarooms_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOME = "nome"
        private const val KEY_CARGO = "cargo"
    }

    // Persiste os dados do usuário após um login bem-sucedido.
    fun salvarSessao(
        token: String,
        email: String,
        nome: String,
        cargo: String
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NOME, nome)
            .putString(KEY_CARGO, cargo)
            .apply()
    }

    // Recupera o token armazenado.
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // Recupera o e-mail armazenado.
    fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    // Recupera o nome armazenado.
    fun getNome(): String {
        return prefs.getString(KEY_NOME, "Usuário") ?: "Usuário"
    }

    // Recupera o cargo armazenado.
    fun getCargo(): String {
        return prefs.getString(KEY_CARGO, "") ?: ""
    }

    // Verifica se existe um token válido armazenado.
    fun estaLogado(): Boolean {
        return !getToken().isNullOrBlank()
    }

    // Remove todos os dados da sessão armazenada.
    fun limparSessao() {
        prefs.edit().clear().apply()
    }
}