package com.botfleet.android.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botfleet.android.data.api.ApiClient
import com.botfleet.android.data.preferences.SessionPreferences
import com.botfleet.android.data.repository.AuthRepository
import com.botfleet.android.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isCheckingSession: Boolean = true,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val botRepository: com.botfleet.android.data.repository.BotRepository,
    private val apiClient: ApiClient,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val serverUrl = sessionPreferences.serverUrl.first()
            val api = apiClient.buildApi(serverUrl)
            authRepository.setApi(api)
            botRepository.setApi(api)

            val token = sessionPreferences.sessionToken.first()
            if (token != null) {
                val result = authRepository.getMe()
                if (result is Result.Success) {
                    _uiState.value = _uiState.value.copy(isCheckingSession = false, isSuccess = true)
                    return@launch
                }
            }
            _uiState.value = _uiState.value.copy(isCheckingSession = false)
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Username and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.login(username, password)) {
                is Result.Success -> {
                    val api = apiClient.buildApi(sessionPreferences.serverUrl.first())
                    authRepository.setApi(api)
                    botRepository.setApi(api)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
