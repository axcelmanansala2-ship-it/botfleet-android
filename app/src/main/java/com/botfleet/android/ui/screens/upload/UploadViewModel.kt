package com.botfleet.android.ui.screens.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botfleet.android.data.repository.BotRepository
import com.botfleet.android.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class UploadUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val selectedFileName: String? = null
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val botRepository: BotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun setSelectedFile(name: String) {
        _uiState.value = _uiState.value.copy(selectedFileName = name, error = null)
    }

    fun upload(file: File, name: String) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Bot name is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = botRepository.uploadBot(file, name)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                is Result.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }
}
