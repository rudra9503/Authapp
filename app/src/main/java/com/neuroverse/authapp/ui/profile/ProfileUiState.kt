package com.neuroverse.authapp.ui.profile

import com.neuroverse.authapp.domain.model.User

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val loggedOut: Boolean = false
)
