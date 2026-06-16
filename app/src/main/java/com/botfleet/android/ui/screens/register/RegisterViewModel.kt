package com.botfleet.android.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botfleet.android.data.api.ApiClient
import com.botfleet.android.data.preferences.SessionPreferences
import com.botfleet.android.data.repository.AuthRepository
import com.botfleet.android.data.repository.BotRepository
import com.botfleet.android.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val botRepository: BotRepository,
    private val apiClient: ApiClient,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "All fields are required")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Passwords do not match")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val serverUrl = sessionPreferences.serverUrl.first()
            val api = apiClient.buildApi(serverUrl)
            authRepository.setApi(api)
            botRepository.setApi(api)
            when (val result = authRepository.register(username, email, password)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }
}
