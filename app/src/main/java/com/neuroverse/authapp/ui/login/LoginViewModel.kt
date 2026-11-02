package com.neuroverse.authapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.repository.AuthRepository
import com.neuroverse.authapp.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }

    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return

        val emailError = Validators.validateEmail(current.email)
        // Login only requires a value: the password policy is not exposed here.
        val passwordError = Validators.validateRequired(current.password)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError, formError = null)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }

        viewModelScope.launch {
            val result = repository.login(
                email = current.email.trim(),
                password = current.password
            )
            when (result) {
                is AuthResult.Success ->
                    _uiState.update { it.copy(isLoading = false, loggedIn = true) }
                is AuthResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, formError = result.error) }
            }
        }
    }
}
