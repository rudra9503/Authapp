package com.neuroverse.authapp.domain.repository

import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.model.User

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): AuthResult<User>
    suspend fun login(email: String, password: String): AuthResult<User>
    suspend fun sendPasswordReset(email: String): AuthResult<Unit>
    suspend fun currentUser(): User?
    suspend fun logout()
}
