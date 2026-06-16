package com.botfleet.android.ui.screens.botdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botfleet.android.data.api.ApiClient
import com.botfleet.android.data.api.models.Bot
import com.botfleet.android.data.preferences.SessionPreferences
import com.botfleet.android.data.repository.BotRepository
import com.botfleet.android.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject

data class BotDetailUiState(
    val bot: Bot? = null,
    val logs: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isActionLoading: Boolean = false,
    val error: String? = null,
    val wsConnected: Boolean = false,
    val installStatus: String? = null
)

@HiltViewModel
class BotDetailViewModel @Inject constructor(
    private val botRepository: BotRepository,
    private val apiClient: ApiClient,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(BotDetailUiState())
    val uiState: StateFlow<BotDetailUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var webSocket: WebSocket? = null
    private var currentBotId: String = ""

    fun init(botId: String) {
        currentBotId = botId
        loadBot(botId)
        startPolling(botId)
        connectWebSocket(botId)
    }

    private fun loadBot(botId: String) {
        viewModelScope.launch {
            when (val result = botRepository.getBot(botId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(bot = result.data, isLoading = false)
                    if (result.data.status == "installing") {
                        checkInstallStatus(botId)
                    }
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    private fun startPolling(botId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                when (val result = botRepository.getBot(botId)) {
                    is Result.Success -> _uiState.value = _uiState.value.copy(bot = result.data)
                    is Result.Error -> {}
                }
            }
        }
    }

    private fun connectWebSocket(botId: String) {
        viewModelScope.launch {
            val baseUrl = sessionPreferences.serverUrl.first()
            val token = sessionPreferences.sessionToken.first() ?: return@launch
            val wsUrl = baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
                .trimEnd('/')
            val request = Request.Builder()
                .url("$wsUrl/ws/logs/$botId?token=$token")
                .build()

            val client = OkHttpClient()
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _uiState.value = _uiState.value.copy(wsConnected = true)
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val json = JSONObject(text)
                        when (json.optString("type")) {
                            "history" -> {
                                val arr = json.getJSONArray("lines")
                                val lines = (0 until arr.length()).map { arr.getString(it) }
                                _uiState.value = _uiState.value.copy(logs = lines)
                            }
                            "logs" -> {
                                val arr = json.getJSONArray("lines")
                                val newLines = (0 until arr.length()).map { arr.getString(it) }
                                val updated = (_uiState.value.logs + newLines).takeLast(500)
                                _uiState.value = _uiState.value.copy(logs = updated)
                            }
                        }
                    } catch (_: Exception) {}
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _uiState.value = _uiState.value.copy(wsConnected = false)
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _uiState.value = _uiState.value.copy(wsConnected = false)
                    viewModelScope.launch {
                        val logs = botRepository.getBotLogs(currentBotId)
                        if (logs is Result.Success) {
                            _uiState.value = _uiState.value.copy(logs = logs.data.logs)
                        }
                    }
                }
            })
        }
    }

    private suspend fun checkInstallStatus(botId: String) {
        when (val result = botRepository.getInstallStatus(botId)) {
            is Result.Success -> _uiState.value = _uiState.value.copy(installStatus = result.data.status)
            is Result.Error -> {}
        }
    }

    fun startBot() {
        val bot = _uiState.value.bot ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = botRepository.startBot(bot.id)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(bot = result.data, isActionLoading = false)
                is Result.Error -> _uiState.value = _uiState.value.copy(isActionLoading = false, error = result.message)
            }
        }
    }

    fun stopBot() {
        val bot = _uiState.value.bot ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = botRepository.stopBot(bot.id)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(bot = result.data, isActionLoading = false)
                is Result.Error -> _uiState.value = _uiState.value.copy(isActionLoading = false, error = result.message)
            }
        }
    }

    fun restartBot() {
        val bot = _uiState.value.bot ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true)
            when (val result = botRepository.restartBot(bot.id)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(bot = result.data, isActionLoading = false)
                is Result.Error -> _uiState.value = _uiState.value.copy(isActionLoading = false, error = result.message)
            }
        }
    }

    fun clearLogs() {
        val bot = _uiState.value.bot ?: return
        viewModelScope.launch {
            botRepository.clearBotLogs(bot.id)
            _uiState.value = _uiState.value.copy(logs = emptyList())
        }
    }

    fun updateAutoRestart(enabled: Boolean) {
        val bot = _uiState.value.bot ?: return
        viewModelScope.launch {
            when (val result = botRepository.updateBot(bot.id, autoRestart = enabled)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(bot = result.data)
                is Result.Error -> {}
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        webSocket?.close(1000, "ViewModel cleared")
        super.onCleared()
    }
}
