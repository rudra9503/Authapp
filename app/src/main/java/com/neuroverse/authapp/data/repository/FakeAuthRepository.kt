package com.neuroverse.authapp.data.repository

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.model.User
import com.neuroverse.authapp.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.util.UUID

/**
 * Temporary in-memory implementation used until the auth provider is approved.
 * Not thread-safe and not persistent: all data is lost when the process dies.
 */
class FakeAuthRepository(
    private val latencyMs: Long = 400L
) : AuthRepository {

    private data class Account(val user: User, val passwordHash: String)

    private val accounts = mutableMapOf<String, Account>() // key: normalised email
    private var signedIn: User? = null

    /** Flip to true to exercise the network-error path. */
    var simulateNetworkError: Boolean = false

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AuthResult<User> {
        delay(latencyMs)
        if (simulateNetworkError) return AuthResult.Failure(AuthError.NETWORK)

        val key = normalise(email)
        if (accounts.containsKey(key)) {
            return AuthResult.Failure(AuthError.EMAIL_ALREADY_IN_USE)
        }
        val user = User(uid = UUID.randomUUID().toString(), name = name.trim(), email = key)
        accounts[key] = Account(user, hash(password))
        signedIn = user
        return AuthResult.Success(user)
    }

    override suspend fun login(email: String, password: String): AuthResult<User> {
        delay(latencyMs)
        if (simulateNetworkError) return AuthResult.Failure(AuthError.NETWORK)

        val account = accounts[normalise(email)]
        if (account == null || account.passwordHash != hash(password)) {
            // Same error for unknown email and wrong password, on purpose.
            return AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
        }
        signedIn = account.user
        return AuthResult.Success(account.user)
    }

    override suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        delay(latencyMs)
        if (simulateNetworkError) return AuthResult.Failure(AuthError.NETWORK)
        // Always succeeds so the response never reveals whether an account exists.
        return AuthResult.Success(Unit)
    }

    override suspend fun currentUser(): User? = signedIn

    override suspend fun logout() {
        signedIn = null
    }

    private fun normalise(email: String) = email.trim().lowercase()

    private fun hash(password: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
