package com.example.agend.gemini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class GeminiChat(private val instrucao: String) {
    private val historico = mutableListOf<JSONObject>()

    suspend fun enviar(chave: String, mensagem: String): String? {
        val pergunta = conteudo(mensagem, "user")
        val conversa = JSONArray(historico + pergunta)
        val resposta = withContext(Dispatchers.IO) {
            val requisicao = JSONObject()
                .put("systemInstruction", conteudo(instrucao))
                .put("contents", conversa)
                .put("generationConfig", JSONObject().put("maxOutputTokens", 1024))
            gerar(chave, requisicao)
        }
        if (resposta != null) {
            historico.add(pergunta)
            historico.add(conteudo(resposta, "model"))
            if (historico.size > 16) historico.subList(0, 2).clear()
        }
        return resposta
    }

    private fun gerar(chave: String, requisicao: JSONObject): String? {
        val conexao = URL(ENDPOINT).openConnection(Proxy.NO_PROXY) as HttpsURLConnection
        try {
            conexao.requestMethod = "POST"
            conexao.connectTimeout = 10_000
            conexao.readTimeout = 30_000
            conexao.instanceFollowRedirects = false
            conexao.doOutput = true
            conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexao.setRequestProperty("x-goog-api-key", chave)
            conexao.outputStream.use { it.write(requisicao.toString().toByteArray(Charsets.UTF_8)) }
            val codigo = conexao.responseCode
            if (codigo !in 200..299) {
                val motivo = when (codigo) {
                    400, 401, 403 -> "O Google recusou a solicitação. Confira a chave e o projeto nas Configurações."
                    429 -> "O Gemini atingiu o limite de uso da chave. Tente mais tarde."
                    else -> "O Gemini está indisponível. Tente novamente em instantes."
                }
                throw GeminiApiException(motivo)
            }

            val resposta = conexao.inputStream.bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
            val partes = resposta.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts") ?: return null
            return (0 until partes.length()).mapNotNull { indice ->
                val parte = partes.optJSONObject(indice) ?: return@mapNotNull null
                if (parte.optBoolean("thought")) null else parte.optString("text").takeIf { it.isNotBlank() }
            }.joinToString("\n").ifBlank { null }
        } finally {
            conexao.disconnect()
        }
    }

    private fun conteudo(texto: String, papel: String? = null) = JSONObject().apply {
        if (papel != null) put("role", papel)
        put("parts", JSONArray().put(JSONObject().put("text", texto)))
    }

    companion object {
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent"
    }
}

internal class GeminiApiException(val mensagemUsuario: String) : Exception()
