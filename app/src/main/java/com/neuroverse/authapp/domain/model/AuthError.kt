package com.neuroverse.authapp.domain.model

/** Provider-neutral failures. Never expose raw provider messages to the UI. */
enum class AuthError {
    EMAIL_ALREADY_IN_USE,
    INVALID_CREDENTIALS, // wrong password OR unknown email: deliberately not distinguished
    NETWORK,
    TOO_MANY_REQUESTS,
    UNKNOWN
}
