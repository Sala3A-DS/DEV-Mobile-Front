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
    val turma: String,
    // ID da opção de uso escolhida pelo professor.
    val opcaoUsoId: String
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
    // Nome da finalidade escolhida.
    val opcaoUsoId: String?,
    val opcaoUsoNome: String?,
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
//Admin
data class ConfiguracaoSistemaResponse(
    val sabadoLetivo: Boolean
)

data class SabadoLetivoRequest(
    val sabadoLetivo: Boolean
)

// Resposta da API para uma opção de uso cadastrada pelo admin.
// Exemplo: Aula prática, Reunião, Prova, Palestra.
data class OpcaoUsoResponse(
    val id: String?,
    val nome: String,
    val ativa: Boolean,
    val criadoPor: String?,
    val criadoEm: String?
)

// Request usado para cadastrar uma nova opção de uso.
data class OpcaoUsoRequest(
    val nome: String
)

// Request usado para ativar ou desativar uma opção de uso.
data class OpcaoUsoStatusRequest(
    val ativa: Boolean
)

// --- PERÍODOS DE AULA ---

// Resposta da API para um período/aula configurado pelo admin.
// Exemplo: 1ª aula - 07:00 às 07:50.
data class PeriodoAulaResponse(
    val id: String?,
    val numero: Int,
    val horarioInicio: String,
    val horarioFim: String,
    val ativo: Boolean,
    val criadoPor: String?,
    val criadoEm: String?
)

// Request usado para cadastrar um novo período de aula.
data class PeriodoAulaRequest(
    val numero: Int,
    val horarioInicio: String,
    val horarioFim: String
)

// Request usado para ativar ou desativar um período de aula.
data class PeriodoAulaStatusRequest(
    val ativo: Boolean
)

// Request usado para gerar automaticamente os períodos de aula.
// Exemplo: 6 aulas, começando 07:00, com 50 minutos cada.
data class GerarPeriodosRequest(
    val quantidadeAulas: Int,
    val horarioInicio: String,
    val duracaoMinutos: Int,
    val intervaloAposAula: Int?,
    val duracaoIntervaloMinutos: Int?
)