package com.botfleet.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botfleet.android.data.api.ApiClient
import com.botfleet.android.data.preferences.SessionPreferences
import com.botfleet.android.data.repository.AuthRepository
import com.botfleet.android.data.repository.BotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sessionPreferences: SessionPreferences,
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
    private val botRepository: BotRepository
) : ViewModel() {

    val serverUrl: StateFlow<String> = sessionPreferences.serverUrl.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "https://your-botfleet-server.replit.app"
    )

    fun saveServerUrl(url: String) {
        viewModelScope.launch {
            sessionPreferences.saveServerUrl(url)
            val api = apiClient.buildApi(url)
            authRepository.setApi(api)
            botRepository.setApi(api)
        }
    }
}
