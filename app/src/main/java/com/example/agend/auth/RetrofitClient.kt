package com.example.agend.auth

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://dev-mobile-back.onrender.com/"
    
    // ATUALIZADO: Variável global para guardar o Token quando o usuário logar
    var token: String? = null

    // ATUALIZADO: Isso funciona como um "carimbo". Se tivermos um Token, ele carimba em todas as requisições.
    private val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient) // Usando o cliente com Token
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: AuthApi = retrofit.create(AuthApi::class.java)
}
