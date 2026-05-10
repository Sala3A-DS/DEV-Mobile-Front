package com.example.agend.auth

// --- LOGIN & REGISTRO ---
data class LoginRequest(val email: String, val senha: String)

data class RegisterRequest(val nome: String, val email: String, val senha: String, val cargo: String)

// ATUALIZADO: O back-end agora devolve um Token JWT além do usuário
data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String?, // ATUALIZADO: No Firebase o ID é String (ex: "yX8aB...")
    val nome: String,
    val email: String,
    val cargo: String
)

// --- AS 3 ETAPAS DE RECUPERAÇÃO DE SENHA ---
data class ForgotPasswordRequest(val email: String) // Passo 1: Pede o código
data class VerifyCodeRequest(val email: String, val codigo: String) // Passo 2: Digita o código
data class ResetPasswordRequest(val email: String, val novaSenha: String) // Passo 3: Nova senha

// --- AGENDAMENTOS ---
data class BookingRequest(
    val nomeFuncionario: String,
    val spaceId: Int,
    val dataHora: String
)

data class BookingResponse(
    val id: String?, // ATUALIZADO: Ajustado para o padrão Firebase
    val nomeFuncionario: String,
    val spaceId: Int,
    val dataHora: String
)

// --- SALAS ---

data class SalaRequest(
    val nome: String,
    val bloco: String,
    val capacidade: Int,
    val recursos: List<String>
)

data class SalaResponse(
    val id: String?,
    val nome: String,
    val bloco: String,
    val capacidade: Int,
    val recursos: List<String>,
    val diretorEmail: String,
    val ativa: Boolean,
    val criadoEm: String
)