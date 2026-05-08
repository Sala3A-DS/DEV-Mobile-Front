package com.example.agend.auth

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://dev-mobile-back.onrender.com/"
    
    // Variável para guardar o token na memória (ou carregar do SharedPreferences)
    var token: String? = null

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        
        // Se tivermos um token, adicionamos no cabeçalho de todas as chamadas
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        chain.proceed(requestBuilder.build())
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // Adicionamos o cliente com suporte a Token
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AuthApi = retrofit.create(AuthApi::class.java)
}
