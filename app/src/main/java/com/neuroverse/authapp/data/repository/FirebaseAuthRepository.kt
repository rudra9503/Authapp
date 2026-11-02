package com.neuroverse.authapp.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.model.User
import com.neuroverse.authapp.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): AuthResult<User> {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        return try {
            val created = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val fbUser = created.user ?: return AuthResult.Failure(AuthError.UNKNOWN)
            try {
                val request = UserProfileChangeRequest.Builder().setDisplayName(cleanName).build()
                fbUser.updateProfile(request).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Account exists; name update failed. Still treat as success.
            }
            AuthResult.Success(User(fbUser.uid, cleanName, fbUser.email ?: cleanEmail))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(mapError(e))
        }
    }

    override suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val fbUser = result.user ?: return AuthResult.Failure(AuthError.UNKNOWN)
            AuthResult.Success(fbUser.toUser())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(mapError(e))
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidUserException) {
            // Do not reveal whether an account exists.
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Failure(mapError(e))
        }
    }

    override suspend fun currentUser(): User? = auth.currentUser?.toUser()

    override suspend fun logout() {
        auth.signOut()
    }

    private fun FirebaseUser.toUser() = User(
        uid = uid,
        name = displayName.orEmpty(),
        email = email.orEmpty()
    )

    private fun mapError(e: Exception): AuthError = when (e) {
        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
        is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
        is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
        is FirebaseNetworkException -> AuthError.NETWORK
        is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
        else -> AuthError.UNKNOWN
    }
}
