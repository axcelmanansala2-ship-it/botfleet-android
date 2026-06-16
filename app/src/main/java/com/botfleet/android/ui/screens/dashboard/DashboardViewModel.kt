package com.botfleet.android.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botfleet.android.data.api.models.Bot
import com.botfleet.android.data.api.models.BotStats
import com.botfleet.android.data.repository.AuthRepository
import com.botfleet.android.data.repository.BotRepository
import com.botfleet.android.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val bots: List<Bot> = emptyList(),
    val stats: BotStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionLoadingBotId: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val botRepository: BotRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                loadData()
                delay(5000)
            }
        }
    }

    private suspend fun loadData() {
        when (val result = botRepository.listBots()) {
            is Result.Success -> _uiState.value = _uiState.value.copy(bots = result.data, isLoading = false, error = null)
            is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
        }
        when (val result = botRepository.getBotStats()) {
            is Result.Success -> _uiState.value = _uiState.value.copy(stats = result.data)
            is Result.Error -> {}
        }
    }

    fun refresh() {
        viewModelScope.launch { loadData() }
    }

    fun startBot(botId: String) = botAction(botId) { botRepository.startBot(botId) }
    fun stopBot(botId: String) = botAction(botId) { botRepository.stopBot(botId) }
    fun restartBot(botId: String) = botAction(botId) { botRepository.restartBot(botId) }

    fun deleteBot(botId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionLoadingBotId = botId)
            botRepository.deleteBot(botId)
            loadData()
            _uiState.value = _uiState.value.copy(actionLoadingBotId = null)
        }
    }

    private fun botAction(botId: String, action: suspend () -> Result<Bot>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionLoadingBotId = botId)
            when (val result = action()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        bots = _uiState.value.bots.map { if (it.id == botId) result.data else it },
                        actionLoadingBotId = null
                    )
                    loadData()
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    actionLoadingBotId = null,
                    error = result.message
                )
            }
        }
    }

    suspend fun logout() {
        authRepository.logout()
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
