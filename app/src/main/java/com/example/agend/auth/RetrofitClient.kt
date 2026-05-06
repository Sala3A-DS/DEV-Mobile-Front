package com.example.agend.auth

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Se o seu Back-end está rodando no IntelliJ (sua máquina), o emulador do Android
    // precisa deste IP especial (10.0.2.2) para acessar o localhost.
    // Quando você for para a nuvem, você trocará isso pela URL do Railway.
    private const val BASE_URL = "https://dev-mobile-back.onrender.com/"

    // Aqui nós "criamos o objeto" do Retrofit com a configuração base
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        // O Gson é quem converte magicamente as Kotlin Data Classes em texto JSON
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Essa variável 'api' é a que você vai usar nas telas do seu App para chamar os métodos
    val api: AuthApi = retrofit.create(AuthApi::class.java)
}