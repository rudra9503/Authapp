package com.neuroverse.authapp.ui.forgot

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.validation.FieldError

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: FieldError? = null,
    val formError: AuthError? = null,
    val isLoading: Boolean = false,
    val resetSent: Boolean = false
)
