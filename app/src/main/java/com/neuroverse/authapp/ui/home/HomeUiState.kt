package com.neuroverse.authapp.ui.home

import com.neuroverse.authapp.domain.model.User

data class HomeUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val loggedOut: Boolean = false
)
