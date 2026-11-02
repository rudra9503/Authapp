package com.neuroverse.authapp.ui.register

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

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, nameError = null, formError = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }

    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return

        val nameError = Validators.validateName(current.name)
        val emailError = Validators.validateEmail(current.email)
        val passwordError = Validators.validateNewPassword(current.password)

        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    formError = null
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }

        viewModelScope.launch {
            val result = repository.register(
                name = current.name.trim(),
                email = current.email.trim(),
                password = current.password
            )
            when (result) {
                is AuthResult.Success ->
                    _uiState.update { it.copy(isLoading = false, registered = true) }
                is AuthResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, formError = result.error) }
            }
        }
    }
}
