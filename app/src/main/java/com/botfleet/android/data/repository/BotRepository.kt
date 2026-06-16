package com.botfleet.android.data.repository

import com.botfleet.android.data.api.BotFleetApi
import com.botfleet.android.data.api.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BotRepository @Inject constructor() {
    private var api: BotFleetApi? = null

    fun setApi(api: BotFleetApi) {
        this.api = api
    }

    suspend fun listBots(): Result<List<Bot>> = safeCall { api!!.listBots() }
    suspend fun getBotStats(): Result<BotStats> = safeCall { api!!.getBotStats() }
    suspend fun getBot(botId: String): Result<Bot> = safeCall { api!!.getBot(botId) }
    suspend fun startBot(botId: String): Result<Bot> = safeCall { api!!.startBot(botId) }
    suspend fun stopBot(botId: String): Result<Bot> = safeCall { api!!.stopBot(botId) }
    suspend fun restartBot(botId: String): Result<Bot> = safeCall { api!!.restartBot(botId) }
    suspend fun deleteBot(botId: String): Result<SuccessResponse> = safeCall { api!!.deleteBot(botId) }
    suspend fun getBotLogs(botId: String): Result<LogsResponse> = safeCall { api!!.getBotLogs(botId) }
    suspend fun clearBotLogs(botId: String): Result<SuccessResponse> = safeCall { api!!.clearBotLogs(botId) }
    suspend fun getSystemStats(): Result<SystemStats> = safeCall { api!!.getSystemStats() }
    suspend fun getInstallStatus(botId: String): Result<InstallStatus> = safeCall { api!!.getInstallStatus(botId) }

    suspend fun updateBot(botId: String, name: String? = null, autoRestart: Boolean? = null): Result<Bot> =
        safeCall { api!!.updateBot(botId, UpdateBotRequest(name, autoRestart)) }

    suspend fun uploadBot(file: File, name: String): Result<Bot> {
        return try {
            val mediaType = if (file.name.endsWith(".zip")) "application/zip" else "text/x-python"
            val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = api!!.uploadBot(filePart, namePart)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error(parseError(response.errorBody()?.string()))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Upload failed")
        }
    }

    private suspend fun <T> safeCall(call: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error(parseError(response.errorBody()?.string()))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    private fun parseError(json: String?): String {
        if (json == null) return "Unknown error"
        return try {
            val obj = org.json.JSONObject(json)
            val detail = obj.opt("detail")
            if (detail is org.json.JSONObject) detail.optString("error", "Unknown error")
            else detail?.toString() ?: obj.optString("error", "Unknown error")
        } catch (_: Exception) {
            json
        }
    }
}
