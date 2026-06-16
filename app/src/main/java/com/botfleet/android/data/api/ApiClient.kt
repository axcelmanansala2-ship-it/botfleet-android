package com.botfleet.android.data.api

import com.botfleet.android.data.preferences.SessionPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    private val sessionPreferences: SessionPreferences
) {
    private var _baseUrl: String = "https://your-botfleet-server.replit.app/"
    private var _retrofit: Retrofit? = null
    private var _api: BotFleetApi? = null

    private fun buildOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = runBlocking { sessionPreferences.sessionToken.first() }
                val request = chain.request().newBuilder().apply {
                    if (token != null) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun buildApi(baseUrl: String): BotFleetApi {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        _baseUrl = url
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(buildOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        _retrofit = retrofit
        val api = retrofit.create(BotFleetApi::class.java)
        _api = api
        return api
    }

    fun getApi(): BotFleetApi? = _api
    fun getBaseUrl(): String = _baseUrl
}
