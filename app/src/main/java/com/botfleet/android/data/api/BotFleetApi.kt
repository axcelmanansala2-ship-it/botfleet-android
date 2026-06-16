package com.botfleet.android.data.api

import com.botfleet.android.data.api.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface BotFleetApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<SuccessResponse>

    @GET("api/auth/me")
    suspend fun me(): Response<UserResponse>

    @GET("api/bots")
    suspend fun listBots(): Response<List<Bot>>

    @GET("api/bots/stats")
    suspend fun getBotStats(): Response<BotStats>

    @Multipart
    @POST("api/bots/upload")
    suspend fun uploadBot(
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody
    ): Response<Bot>

    @GET("api/bots/{botId}")
    suspend fun getBot(@Path("botId") botId: String): Response<Bot>

    @DELETE("api/bots/{botId}")
    suspend fun deleteBot(@Path("botId") botId: String): Response<SuccessResponse>

    @POST("api/bots/{botId}/start")
    suspend fun startBot(@Path("botId") botId: String): Response<Bot>

    @POST("api/bots/{botId}/stop")
    suspend fun stopBot(@Path("botId") botId: String): Response<Bot>

    @POST("api/bots/{botId}/restart")
    suspend fun restartBot(@Path("botId") botId: String): Response<Bot>

    @PATCH("api/bots/{botId}")
    suspend fun updateBot(
        @Path("botId") botId: String,
        @Body request: UpdateBotRequest
    ): Response<Bot>

    @GET("api/bots/{botId}/logs")
    suspend fun getBotLogs(@Path("botId") botId: String): Response<LogsResponse>

    @DELETE("api/bots/{botId}/logs")
    suspend fun clearBotLogs(@Path("botId") botId: String): Response<SuccessResponse>

    @GET("api/bots/{botId}/install-status")
    suspend fun getInstallStatus(@Path("botId") botId: String): Response<InstallStatus>

    @GET("api/system/stats")
    suspend fun getSystemStats(): Response<SystemStats>
}
