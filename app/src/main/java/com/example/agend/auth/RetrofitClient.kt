package com.example.agend.auth

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://dev-mobile-back.onrender.com/"

    // Guarda o token JWT quando o usuário faz login.
    var token: String? = null

    private val httpClient = OkHttpClient.Builder()

        // Aumenta o tempo para conectar no servidor.
        // Ajuda quando o Render demora para acordar.
        .connectTimeout(60, TimeUnit.SECONDS)

        // Aumenta o tempo para esperar resposta do servidor.
        // Ajuda no forgot-password, porque ele ainda envia e-mail.
        .readTimeout(60, TimeUnit.SECONDS)

        // Aumenta o tempo para enviar dados ao servidor.
        .writeTimeout(60, TimeUnit.SECONDS)

        .addInterceptor { chain ->

            // Pega a requisição original.
            val originalRequest = chain.request()

            // Pega o caminho da URL.
            val path = originalRequest.url().encodedPath()

            // Rotas públicas que não precisam de token.
            val isPublicAuthRoute =
                path.contains("/api/auth/login") ||
                        path.contains("/api/auth/register") ||
                        path.contains("/api/auth/forgot-password") ||
                        path.contains("/api/auth/verify-code") ||
                        path.contains("/api/auth/reset-password")

            val requestBuilder = originalRequest.newBuilder()

            // Só adiciona token em rotas protegidas.
            if (!isPublicAuthRoute) {
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
            }

            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AuthApi = retrofit.create(AuthApi::class.java)
}