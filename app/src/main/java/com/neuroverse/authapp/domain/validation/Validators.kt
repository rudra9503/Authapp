package com.neuroverse.authapp.domain.validation

enum class FieldError {
    BLANK,
    NAME_TOO_SHORT,
    INVALID_EMAIL,
    PASSWORD_TOO_SHORT
}

/** Pure-Kotlin input rules. A null result means the value is valid. */
object Validators {
    const val MIN_NAME_LENGTH = 2
    const val MIN_PASSWORD_LENGTH = 8 // assumption: confirm with NeuroVerse

    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun validateName(name: String): FieldError? = when {
        name.isBlank() -> FieldError.BLANK
        name.trim().length < MIN_NAME_LENGTH -> FieldError.NAME_TOO_SHORT
        else -> null
    }

    fun validateEmail(email: String): FieldError? = when {
        email.isBlank() -> FieldError.BLANK
        !EMAIL_REGEX.matches(email.trim()) -> FieldError.INVALID_EMAIL
        else -> null
    }

    /** Registration: enforces the password policy. */
    fun validateNewPassword(password: String): FieldError? = when {
        password.isEmpty() -> FieldError.BLANK
        password.length < MIN_PASSWORD_LENGTH -> FieldError.PASSWORD_TOO_SHORT
        else -> null
    }

    /** Login: only requires a value, so the policy isn't exposed. */
    fun validateRequired(value: String): FieldError? =
        if (value.isEmpty()) FieldError.BLANK else null
}
