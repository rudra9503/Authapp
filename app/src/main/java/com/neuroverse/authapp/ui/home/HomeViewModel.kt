package com.neuroverse.authapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroverse.authapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = repository.currentUser()
            // No signed-in user: this screen must not be shown (basic route guard).
            _uiState.update {
                it.copy(user = user, isLoading = false, loggedOut = user == null)
            }
        }
    }

    fun onLogout() {
        val current = _uiState.value
        if (current.isLoading || current.loggedOut) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(user = null, isLoading = false, loggedOut = true) }
        }
    }
}
