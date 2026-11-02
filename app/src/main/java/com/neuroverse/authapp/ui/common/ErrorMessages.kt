package com.neuroverse.authapp.ui.common

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.validation.FieldError
import com.neuroverse.authapp.domain.validation.Validators

/** User-facing text for validation errors. Never includes raw provider messages. */
fun fieldErrorMessage(error: FieldError): String = when (error) {
    FieldError.BLANK -> "This field is required"
    FieldError.NAME_TOO_SHORT -> "Name must be at least ${Validators.MIN_NAME_LENGTH} characters"
    FieldError.INVALID_EMAIL -> "Enter a valid email address"
    FieldError.PASSWORD_TOO_SHORT -> "Password must be at least ${Validators.MIN_PASSWORD_LENGTH} characters"
}

/** User-facing text for authentication failures. Deliberately generic for login. */
fun authErrorMessage(error: AuthError): String = when (error) {
    AuthError.EMAIL_ALREADY_IN_USE -> "An account with this email already exists"
    AuthError.INVALID_CREDENTIALS -> "Incorrect email or password"
    AuthError.NETWORK -> "No internet connection. Check your connection and try again."
    AuthError.TOO_MANY_REQUESTS -> "Too many attempts. Please try again later."
    AuthError.UNKNOWN -> "Something went wrong. Please try again."
}
