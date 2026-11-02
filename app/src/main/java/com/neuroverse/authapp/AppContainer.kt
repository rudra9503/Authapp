package com.neuroverse.authapp

import com.neuroverse.authapp.data.repository.FakeAuthRepository
import com.neuroverse.authapp.domain.repository.AuthRepository

/**
 * Temporary manual dependency holder. Swapped for the approved provider later;
 * only this file needs to change to switch implementations.
 */
object AppContainer {
    val authRepository: AuthRepository by lazy { FakeAuthRepository() }
}
