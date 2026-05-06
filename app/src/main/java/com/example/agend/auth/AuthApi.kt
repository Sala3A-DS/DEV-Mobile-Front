package com.example.agend.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    // A rota que você criou lá no SchoolController do IntelliJ
    @POST("api/bookings")
    fun makeBooking(@Body booking: BookingRequest): Call<String>

    @GET("api/bookings")
    fun listarAgendamentos(): Call<List<BookingResponse>>
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<UserResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

    @POST("api/auth/forgot-password")
    fun resetPassword(@Body request: ForgotPasswordRequest): Call<String>
}