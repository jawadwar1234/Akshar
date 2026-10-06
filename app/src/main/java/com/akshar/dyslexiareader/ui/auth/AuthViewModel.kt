package com.akshar.dyslexiareader.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.dyslexiareader.data.repository.UserPreferencesRepository
import com.akshar.dyslexiareader.domain.model.UserSettings
import com.akshar.dyslexiareader.util.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isRegisterMode = !_uiState.value.isRegisterMode,
            errorMessage = null
        )
    }

    fun acceptParentalConsent() {
        viewModelScope.launch {
            preferencesRepository.setParentalConsent(true)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setAppLanguage(language)
        }
    }

    fun loginWithEmail() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            // Simulated / offline parent auth token
            val parentId = "parent_${email.hashCode()}"
            preferencesRepository.setParentAccount(parentId, isOffline = false)
            _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val parentId = "google_parent_${UUID.randomUUID().toString().take(8)}"
            preferencesRepository.setParentAccount(parentId, isOffline = false)
            _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
        }
    }

    fun continueInOfflineMode() {
        viewModelScope.launch {
            val offlineParentId = "offline_local_parent"
            preferencesRepository.setParentAccount(offlineParentId, isOffline = true)
            _uiState.value = _uiState.value.copy(isAuthenticated = true)
        }
    }
}
