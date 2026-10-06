package com.example.agend.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    // Consulta as reservas gerais, podendo filtrar por data.
    @GET("api/reservas/geral")
    fun listarReservasGerais(
        @Query("data") data: String? = null
    ): Call<List<ReservaResponse>>

    // Cria um novo agendamento.
    @POST("api/bookings")
    fun makeBooking(@Body booking: BookingRequest): Call<String>

    // Lista os agendamentos existentes.
    @GET("api/bookings")
    fun listarAgendamentos(): Call<List<BookingResponse>>

    // Realiza o login e retorna os dados da sessão, incluindo o token.
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Cadastra um novo usuário.
    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

    // Solicita o envio do código para recuperação de senha.
    @POST("api/auth/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<String>

    // Valida o código enviado para recuperação de senha.
    @POST("api/auth/verify-code")
    fun verifyCode(@Body request: VerifyCodeRequest): Call<String>

    // Redefine a senha após a validação do código.
    @POST("api/auth/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<String>

    // Cadastra uma nova sala.
    @POST("api/salas")
    fun cadastrarSala(@Body request: SalaRequest): Call<SalaResponse>

    // Lista todas as salas disponíveis.
    @GET("api/salas")
    fun listarSalas(): Call<List<SalaResponse>>

    // Lista as salas cadastradas pelo usuário atual.
    @GET("api/salas/minhas")
    fun listarMinhasSalas(): Call<List<SalaResponse>>

    // Cria uma nova reserva.
    @POST("api/reservas")
    fun criarReserva(
        @Body request: ReservaRequest
    ): Call<ReservaResponse>

    // Consulta as configurações atuais do sistema.
    @GET("api/configuracoes")
    fun buscarConfiguracaoSistema(): Call<ConfiguracaoSistemaResponse>

    // Atualiza a configuração de sábado letivo.
    @PATCH("api/configuracoes/sabado-letivo")
    fun atualizarSabadoLetivo(
        @Body request: SabadoLetivoRequest
    ): Call<ConfiguracaoSistemaResponse>

    // Lista as reservas do usuário atual.
    @GET("api/reservas/minhas")
    fun listarMinhasReservas(): Call<List<ReservaResponse>>

    // Cancela uma reserva pelo identificador.
    @PATCH("api/reservas/{id}/cancelar")
    fun cancelarReserva(
        @Path("id") id: String
    ): Call<ReservaResponse>

    // Consulta a disponibilidade de uma sala em uma data e turno.
    @GET("api/reservas/disponibilidade")
    fun consultarDisponibilidade(
        @Query("salaId") salaId: String,
        @Query("data") data: String,
        @Query("turno") turno: String
    ): Call<List<DisponibilidadeSalaResponse>>

    // Cadastra uma nova opção de uso.
    @POST("api/opcoes-uso")
    fun cadastrarOpcaoUso(
        @Body request: OpcaoUsoRequest
    ): Call<OpcaoUsoResponse>

    // Lista as opções de uso para gerenciamento administrativo.
    @GET("api/opcoes-uso/admin")
    fun listarOpcoesUsoAdmin(): Call<List<OpcaoUsoResponse>>

    // Atualiza o status de uma opção de uso.
    @PATCH("api/opcoes-uso/{id}/status")
    fun alterarStatusOpcaoUso(
        @Path("id") id: String,
        @Body request: OpcaoUsoStatusRequest
    ): Call<OpcaoUsoResponse>

    // Lista somente as opções de uso ativas.
    @GET("api/opcoes-uso")
    fun listarOpcoesUsoAtivas(): Call<List<OpcaoUsoResponse>>

    // Lista os períodos de aula para gerenciamento administrativo.
    @GET("api/periodos-aula/admin")
    fun listarPeriodosAulaAdmin(): Call<List<PeriodoAulaResponse>>

    // Atualiza o status de um período de aula.
    @PATCH("api/periodos-aula/{id}/status")
    fun alterarStatusPeriodoAula(
        @Path("id") id: String,
        @Body request: PeriodoAulaStatusRequest
    ): Call<PeriodoAulaResponse>

    // Gera os períodos de aula conforme a configuração informada.
    @POST("api/periodos-aula/gerar")
    fun gerarPeriodosAula(
        @Body request: GerarPeriodosRequest
    ): Call<List<PeriodoAulaResponse>>
}