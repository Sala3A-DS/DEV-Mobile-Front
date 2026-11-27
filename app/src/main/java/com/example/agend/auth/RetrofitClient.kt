package com.example.agend.auth

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val BASE_URL = com.example.agend.BuildConfig.API_BASE_URL

    // Armazena o token JWT da sessão atual.
    var token: String? = null

    private val httpClient = OkHttpClient.Builder()

        // Define o tempo máximo para estabelecer conexão com o servidor.
        .connectTimeout(5, TimeUnit.SECONDS)

        // Define o tempo máximo para receber a resposta do servidor.
        .readTimeout(5, TimeUnit.SECONDS)

        // Define o tempo máximo para enviar dados ao servidor.
        .writeTimeout(5, TimeUnit.SECONDS)

        // Intercepta as requisições antes do envio.
        .addInterceptor { chain ->

            // Recupera a requisição original.
            val originalRequest = chain.request()

            // Obtém o caminho da URL para identificar a rota.
            val path = originalRequest.url.encodedPath

            // Define as rotas de autenticação que não exigem token.
            val isPublicAuthRoute =
                path.contains("/api/auth/login") ||
                        path.contains("/api/auth/register") ||
                        path.contains("/api/auth/forgot-password") ||
                        path.contains("/api/auth/verify-code") ||
                        path.contains("/api/auth/reset-password")

            val requestBuilder = originalRequest.newBuilder()

            // Adiciona o token somente nas rotas protegidas.
            if (!isPublicAuthRoute) {
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
            }

            // As rotas de envio de e-mail podem aguardar o SMTP.
            val clientChain = when (path) {
                "/api/auth/forgot-password", "/api/auth/reset-password" ->
                    chain.withReadTimeout(30, TimeUnit.SECONDS)
                else -> chain
            }
            clientChain.proceed(requestBuilder.build())
        }
        .build()

    // Configura o Retrofit e os conversores utilizados pela API.
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Disponibiliza a interface da API para as Activities e demais componentes.
    val api: AuthApi = retrofit.create(AuthApi::class.java)
}