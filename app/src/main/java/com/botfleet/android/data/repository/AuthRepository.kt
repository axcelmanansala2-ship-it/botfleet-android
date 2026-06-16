package com.botfleet.android.data.repository

import com.botfleet.android.data.api.BotFleetApi
import com.botfleet.android.data.api.models.AuthResponse
import com.botfleet.android.data.api.models.LoginRequest
import com.botfleet.android.data.api.models.RegisterRequest
import com.botfleet.android.data.api.models.UserResponse
import com.botfleet.android.data.preferences.SessionPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val sessionPreferences: SessionPreferences
) {
    private var api: BotFleetApi? = null

    fun setApi(api: BotFleetApi) {
        this.api = api
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = api!!.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                sessionPreferences.saveToken(body.token)
                Result.Success(body)
            } else {
                Result.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api!!.register(RegisterRequest(username, email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                sessionPreferences.saveToken(body.token)
                Result.Success(body)
            } else {
                Result.Error(parseError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun logout() {
        try { api?.logout() } catch (_: Exception) {}
        sessionPreferences.clearToken()
    }

    suspend fun getMe(): Result<UserResponse> {
        return try {
            val response = api!!.me()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Not authenticated")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return sessionPreferences.sessionToken.first() != null
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
