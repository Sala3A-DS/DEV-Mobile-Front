package com.example.agend.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface AuthApi {
    @GET("api/reservas/geral")
    fun listarReservasGerais(
        @Query("data") data: String? = null
    ): Call<List<ReservaResponse>>


    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<String>

    // --- ROTAS DE SENHA ---
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

    // --- ROTAS DE RESERVAS ---
    @POST("api/reservas")
    fun criarReserva(
        @Body request: ReservaRequest
    ): Call<ReservaResponse>

    @GET("api/configuracoes")
    fun buscarConfiguracaoSistema(): Call<ConfiguracaoSistemaResponse>

    @PATCH("api/configuracoes/sabado-letivo")
    fun atualizarSabadoLetivo(
        @Body request: SabadoLetivoRequest
    ): Call<ConfiguracaoSistemaResponse>

    @GET("api/reservas/minhas")
    fun listarMinhasReservas(): Call<List<ReservaResponse>>

    @PATCH("api/reservas/{id}/cancelar")
    fun cancelarReserva(
        @Path("id") id: String
    ): Call<ReservaResponse>

    @GET("api/reservas/disponibilidade")
    fun consultarDisponibilidade(
        @Query("salaId") salaId: String,
        @Query("data") data: String,
        @Query("turno") turno: String
    ): Call<List<DisponibilidadeSalaResponse>>

    // --- ROTAS DE OPÇÕES DE USO ---
    @POST("api/opcoes-uso")
    fun cadastrarOpcaoUso(
        @Body request: OpcaoUsoRequest
    ): Call<OpcaoUsoResponse>

    @GET("api/opcoes-uso/admin")
    fun listarOpcoesUsoAdmin(): Call<List<OpcaoUsoResponse>>

    @PATCH("api/opcoes-uso/{id}/status")
    fun alterarStatusOpcaoUso(
        @Path("id") id: String,
        @Body request: OpcaoUsoStatusRequest
    ): Call<OpcaoUsoResponse>

    @GET("api/opcoes-uso")
    fun listarOpcoesUsoAtivas(): Call<List<OpcaoUsoResponse>>

    // --- ROTAS DE PERÍODOS DE AULA ---

    @GET("api/periodos-aula/admin")
    fun listarPeriodosAulaAdmin(): Call<List<PeriodoAulaResponse>>

    @PATCH("api/periodos-aula/{id}/status")
    fun alterarStatusPeriodoAula(
        @Path("id") id: String,
        @Body request: PeriodoAulaStatusRequest
    ): Call<PeriodoAulaResponse>

    @POST("api/periodos-aula/gerar")
    fun gerarPeriodosAula(
        @Body request: GerarPeriodosRequest
    ): Call<List<PeriodoAulaResponse>>
}
