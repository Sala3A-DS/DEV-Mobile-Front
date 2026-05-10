package com.example.agend.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/bookings")
    fun makeBooking(@Body booking: BookingRequest): Call<String>

    @GET("api/bookings")
    fun listarAgendamentos(): Call<List<BookingResponse>>

    // ATUALIZADO: Agora retorna o LoginResponse (que contém o Token)
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

    // --- AS 3 ROTAS DE SENHA ATUALIZADAS ---
    @POST("api/auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<String>

    @POST("api/auth/verify-code")
    fun verifyCode(@Body request: VerifyCodeRequest): Call<String>

    @POST("api/auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<String>

    // --- ROTAS DE SALAS ---

    @POST("api/salas")
    fun cadastrarSala(@Body request: SalaRequest): Call<SalaResponse>

    @GET("api/salas")
    fun listarSalas(): Call<List<SalaResponse>>

    @GET("api/salas/minhas")
    fun listarMinhasSalas(): Call<List<SalaResponse>>
}
