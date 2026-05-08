package com.example.agend.auth

// --- LOGIN ---
data class LoginRequest(val email: String, val senha: String)

// ATUALIZADO: Agora o login recebe o Token e os dados do Usuário
data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String?, // No Firebase o ID é String, não Int
    val nome: String,
    val email: String,
    val cargo: String
)

// --- REGISTRO ---
data class RegisterRequest(val nome: String, val email: String, val senha: String, val cargo: String)

// --- RECUPERAÇÃO DE SENHA (3 Passos) ---
data class ForgotPasswordRequest(val email: String) // Passo 1: Só envia e-mail
data class VerifyCodeRequest(val email: String, val codigo: String) // Passo 2: Valida código
data class ResetPasswordRequest(val email: String, val novaSenha: String) // Passo 3: Define senha

// --- AGENDAMENTOS ---
data class BookingRequest(val nomeFuncionario: String, val spaceId: Int, val dataHora: String)
data class BookingResponse(val id: String, val nomeFuncionario: String, val spaceId: Int, val dataHora: String)
