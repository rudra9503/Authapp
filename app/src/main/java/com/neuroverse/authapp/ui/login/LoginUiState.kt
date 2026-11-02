package com.neuroverse.authapp.ui.login

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.validation.FieldError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: FieldError? = null,
    val passwordError: FieldError? = null,
    val formError: AuthError? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val loggedIn: Boolean = false
)
