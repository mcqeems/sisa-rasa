package com.mobile.sisarasa.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.sisarasa.data.repository.AuthRepository
import com.mobile.sisarasa.di.AppContainer
import com.mobile.sisarasa.domain.model.AppUser
import com.mobile.sisarasa.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: AppUser? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(container: AppContainer) : ViewModel() {

    private val repo: AuthRepository = container.authRepository

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.authState.collect { user -> _uiState.value = _uiState.value.copy(currentUser = user) }
        }
    }

    fun login(email: String, password: String) = runAuth { repo.login(email, password) }

    fun register(email: String, password: String, name: String, role: UserRole) =
        runAuth { repo.register(email, password, name, role) }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
        }
    }

    private fun runAuth(block: suspend () -> Result<AppUser>) {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = block()
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = result.exceptionOrNull()?.message,
            )
        }
    }
}
