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
    val nomeEspaco: String,
    val localizacao: String,
    val numeroSala: Int
)

data class SalaResponse(
    val id: String?,
    val nomeEspaco: String,
    val localizacao: String,
    val numeroSala: Int,
    val diretorEmail: String,
    val ativa: Boolean,
    val criadoEm: String
)

// --- RESERVAS ---

data class ReservaRequest(
    val salaId: String,
    val data: String,
    val periodoAula: String,
    val turma: String
)

data class ReservaResponse(
    val id: String?,
    val salaId: String,
    val salaNome: String,
    val professorEmail: String,
    val professorNome: String,
    val data: String,
    val periodoAula: String,
    val horarioInicio: String,
    val horarioFim: String,
    val turma: String,
    val status: String,
    val criadoEm: String
)

data class DisponibilidadeSalaResponse(
    val periodoAula: String,
    val horarioInicio: String,
    val horarioFim: String,
    val disponivel: Boolean,
    val reservaId: String?,
    val professorNome: String?
)