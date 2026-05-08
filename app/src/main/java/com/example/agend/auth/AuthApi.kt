package com.example.agend.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<String>

    // FLUXO DE SENHA ATUALIZADO
    @POST("api/auth/forgot-password")
    suspend fun sendCode(@Body request: ForgotPasswordRequest): Response<String>

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): Response<String>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<String>

    // AGENDAMENTOS
    @POST("api/bookings")
    suspend fun makeBooking(@Body booking: BookingRequest): Response<String>

    @GET("api/bookings")
    suspend fun listarAgendamentos(): Response<List<BookingResponse>>
}
