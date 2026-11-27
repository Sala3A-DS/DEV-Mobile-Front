package com.example.agend.auth

// Modelos utilizados na autenticação e cadastro de usuários.
data class LoginRequest(
    val email: String,
    val senha: String
)

data class RegisterRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cargo: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String?,
    val nome: String,
    val email: String,
    val cargo: String
)

// Modelos utilizados no fluxo de recuperação de senha.
data class ForgotPasswordRequest(
    val email: String
)

data class VerifyCodeRequest(
    val email: String,
    val codigo: String
)

data class ResetPasswordRequest(
    val email: String,
    val novaSenha: String,
    val codigo: String
)

// Modelos utilizados pelos agendamentos legados.
data class BookingRequest(
    val nomeFuncionario: String,
    val spaceId: Int,
    val dataHora: String
)

data class BookingResponse(
    val id: String?,
    val nomeFuncionario: String,
    val spaceId: Int,
    val dataHora: String
)

// Modelos utilizados no cadastro e consulta de salas.
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

// Modelos utilizados na criação e consulta de reservas.
data class ReservaRequest(
    val salaId: String,
    val data: String,
    val periodoAula: String,
    val turma: String,
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

// Modelos utilizados na configuração administrativa do sistema.
data class ConfiguracaoSistemaResponse(
    val sabadoLetivo: Boolean
)

data class SabadoLetivoRequest(
    val sabadoLetivo: Boolean
)

// Modelos utilizados no gerenciamento das opções de uso.
data class OpcaoUsoResponse(
    val id: String?,
    val nome: String,
    val ativa: Boolean,
    val criadoPor: String?,
    val criadoEm: String?
)

data class OpcaoUsoRequest(
    val nome: String
)

data class OpcaoUsoStatusRequest(
    val ativa: Boolean
)

// Modelos utilizados no gerenciamento dos períodos de aula.
data class PeriodoAulaResponse(
    val id: String?,
    val turno: String?,
    val numero: Int,
    val horarioInicio: String,
    val horarioFim: String,
    val ativo: Boolean,
    val criadoPor: String?,
    val criadoEm: String?
)

data class PeriodoAulaStatusRequest(
    val ativo: Boolean
)

// Dados utilizados para gerar automaticamente os períodos de aula.
data class GerarPeriodosRequest(
    val turno: String,
    val quantidadeAulas: Int,
    val horarioInicio: String,
    val duracaoMinutos: Int,
    val intervaloAposAula: Int?,
    val duracaoIntervaloMinutos: Int?
)