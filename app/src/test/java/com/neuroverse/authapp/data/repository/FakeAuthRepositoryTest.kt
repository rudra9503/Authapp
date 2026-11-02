package com.neuroverse.authapp.data.repository

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.model.AuthResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthRepositoryTest {

    private fun newRepo() = FakeAuthRepository(latencyMs = 0L)

    private fun <T> AuthResult<T>.successData(): T {
        assertTrue("Expected Success but was $this", this is AuthResult.Success)
        return (this as AuthResult.Success).data
    }

    // ---- session state ----

    @Test
    fun `no user is signed in initially`() = runBlocking {
        assertNull(newRepo().currentUser())
    }

    @Test
    fun `logout clears the signed in user`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")
        assertNotNull(repo.currentUser())

        repo.logout()

        assertNull(repo.currentUser())
    }

    // ---- register ----

    @Test
    fun `register succeeds and normalises name and email`() = runBlocking {
        val repo = newRepo()

        val user = repo.register("  Asha  ", "  Asha@Example.COM ", "password1").successData()

        assertEquals("Asha", user.name)
        assertEquals("asha@example.com", user.email)
        assertTrue(user.uid.isNotBlank())
    }

    @Test
    fun `register signs the new user in`() = runBlocking {
        val repo = newRepo()

        val user = repo.register("Asha", "asha@example.com", "password1").successData()

        assertEquals(user, repo.currentUser())
    }

    @Test
    fun `register with existing email returns EMAIL_ALREADY_IN_USE`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")

        val result = repo.register("Other", "asha@example.com", "password2")

        assertEquals(AuthResult.Failure(AuthError.EMAIL_ALREADY_IN_USE), result)
    }

    @Test
    fun `duplicate email check ignores case and surrounding spaces`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")

        val result = repo.register("Other", "  ASHA@Example.com ", "password2")

        assertEquals(AuthResult.Failure(AuthError.EMAIL_ALREADY_IN_USE), result)
    }

    // ---- login ----

    @Test
    fun `login with correct credentials succeeds`() = runBlocking {
        val repo = newRepo()
        val registered = repo.register("Asha", "asha@example.com", "password1").successData()
        repo.logout()

        val user = repo.login("asha@example.com", "password1").successData()

        assertEquals(registered, user)
        assertEquals(registered, repo.currentUser())
    }

    @Test
    fun `login email match ignores case and surrounding spaces`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")
        repo.logout()

        val result = repo.login("  ASHA@Example.com ", "password1")

        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `login with wrong password returns INVALID_CREDENTIALS`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")
        repo.logout()

        val result = repo.login("asha@example.com", "wrong-password")

        assertEquals(AuthResult.Failure(AuthError.INVALID_CREDENTIALS), result)
    }

    @Test
    fun `login with unknown email returns the same error as a wrong password`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")
        repo.logout()

        val wrongPassword = repo.login("asha@example.com", "wrong-password")
        val unknownEmail = repo.login("nobody@example.com", "password1")

        assertEquals(wrongPassword, unknownEmail)
        assertEquals(AuthResult.Failure(AuthError.INVALID_CREDENTIALS), unknownEmail)
    }

    @Test
    fun `failed login does not sign anyone in`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")
        repo.logout()

        repo.login("asha@example.com", "wrong-password")

        assertNull(repo.currentUser())
    }

    // ---- password reset ----

    @Test
    fun `password reset for a registered email succeeds`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")

        val result = repo.sendPasswordReset("asha@example.com")

        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `password reset for an unknown email also succeeds`() = runBlocking {
        val result = newRepo().sendPasswordReset("nobody@example.com")

        assertTrue(result is AuthResult.Success)
    }

    // ---- network errors ----

    @Test
    fun `network error is reported by register, login and reset`() = runBlocking {
        val repo = newRepo()
        repo.simulateNetworkError = true
        val expected = AuthResult.Failure(AuthError.NETWORK)

        assertEquals(expected, repo.register("Asha", "asha@example.com", "password1"))
        assertEquals(expected, repo.login("asha@example.com", "password1"))
        assertEquals(expected, repo.sendPasswordReset("asha@example.com"))
    }

    @Test
    fun `register during a network error does not create an account`() = runBlocking {
        val repo = newRepo()
        repo.simulateNetworkError = true
        repo.register("Asha", "asha@example.com", "password1")
        repo.simulateNetworkError = false

        val result = repo.register("Asha", "asha@example.com", "password1")

        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `login during a network error does not sign in`() = runBlocking {
        val repo = newRepo()
        repo.register("Asha", "asha@example.com", "password1")
        repo.logout()
        repo.simulateNetworkError = true

        repo.login("asha@example.com", "password1")

        assertNull(repo.currentUser())
    }
}
