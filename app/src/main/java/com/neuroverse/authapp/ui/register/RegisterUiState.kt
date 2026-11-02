package com.neuroverse.authapp.ui.register

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.validation.FieldError

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: FieldError? = null,
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val formError: AuthError? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val registered: Boolean = false
)
