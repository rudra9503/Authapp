package com.neuroverse.authapp.ui.forgot

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

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return

        val emailError = Validators.validateEmail(current.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError, formError = null) }
            return
        }

        _uiState.update { it.copy(isLoading = true, formError = null) }

        viewModelScope.launch {
            // The repository must report success for unknown emails too,
            // so this screen never reveals which emails are registered.
            when (val result = repository.sendPasswordReset(current.email.trim())) {
                is AuthResult.Success ->
                    _uiState.update { it.copy(isLoading = false, resetSent = true) }
                is AuthResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, formError = result.error) }
            }
        }
    }
}
