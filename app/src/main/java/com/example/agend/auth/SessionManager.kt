package com.example.agend.auth

import android.content.Context

class SessionManager(context: Context) {

    // SharedPreferences é usado para salvar dados simples no celular.
    // Aqui vamos guardar token, nome, e-mail e cargo do usuário logado.
    private val prefs = context.getSharedPreferences("yarooms_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOME = "nome"
        private const val KEY_CARGO = "cargo"
    }

    // Salva os dados da sessão após o login ser feito com sucesso.
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

    // Recupera o token salvo.
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // Recupera o e-mail salvo.
    fun getEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    // Recupera o nome salvo.
    fun getNome(): String {
        return prefs.getString(KEY_NOME, "Usuário") ?: "Usuário"
    }

    // Recupera o cargo salvo.
    fun getCargo(): String {
        return prefs.getString(KEY_CARGO, "") ?: ""
    }

    // Verifica se existe um token salvo.
    // Se existir, o usuário pode ser redirecionado automaticamente.
    fun estaLogado(): Boolean {
        return !getToken().isNullOrBlank()
    }

    // Limpa a sessão no logout ou quando o token expirar.
    fun limparSessao() {
        prefs.edit().clear().apply()
    }
}