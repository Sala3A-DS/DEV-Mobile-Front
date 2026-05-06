package com.example.agend.auth

// O que o Android envia
data class LoginRequest(val email: String, val senha: String)

data class RegisterRequest(val nome: String, val email: String, val senha: String, val cargo: String)

data class ForgotPasswordRequest(val email: String, val novaSenha: String)

// O que o Android recebe de volta após o login com sucesso
data class UserResponse(
    val id: Int,
    val nome: String,
    val email: String,
    val cargo: String
)

data class BookingResponse(
    val id: Int,
    val nomeFuncionario: String,
    val spaceId: Int,
    val dataHora: String
)

// O formato exato que o Spring Boot exige para criar uma reserva
data class BookingRequest(
    val nomeFuncionario: String,
    val spaceId: Int,
    val dataHora: String
)