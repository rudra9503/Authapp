package com.neuroverse.authapp.ui.forgot

import com.neuroverse.authapp.data.repository.FakeAuthRepository
import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.model.User
import com.neuroverse.authapp.domain.repository.AuthRepository
import com.neuroverse.authapp.domain.validation.FieldError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    /** Records calls and returns a configurable result. Optionally waits on a gate. */
    private class StubAuthRepository(
        var resetResult: AuthResult<Unit> = AuthResult.Success(Unit),
        private val gate: CompletableDeferred<Unit>? = null
    ) : AuthRepository {
        var resetCalls = 0
        var lastEmail: String? = null

        override suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
            resetCalls++
            lastEmail = email
            gate?.await()
            return resetResult
        }

        override suspend fun register(name: String, email: String, password: String): AuthResult<User> =
            AuthResult.Failure(AuthError.UNKNOWN)

        override suspend fun login(email: String, password: String): AuthResult<User> =
            AuthResult.Failure(AuthError.UNKNOWN)

        override suspend fun currentUser(): User? = null
        override suspend fun logout() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and idle`() {
        val state = ForgotPasswordViewModel(StubAuthRepository()).uiState.value

        assertEquals(ForgotPasswordUiState(), state)
        assertFalse(state.isLoading)
        assertFalse(state.resetSent)
    }

    @Test
    fun `blank email shows required error and does not call repository`() {
        val repo = StubAuthRepository()
        val vm = ForgotPasswordViewModel(repo)

        vm.onSubmit()

        assertEquals(FieldError.BLANK, vm.uiState.value.emailError)
        assertFalse(vm.uiState.value.resetSent)
        assertEquals(0, repo.resetCalls)
    }

    @Test
    fun `invalid email format shows error and does not call repository`() {
        val repo = StubAuthRepository()
        val vm = ForgotPasswordViewModel(repo)
        vm.onEmailChange("not-an-email")

        vm.onSubmit()

        assertEquals(FieldError.INVALID_EMAIL, vm.uiState.value.emailError)
        assertFalse(vm.uiState.value.resetSent)
        assertEquals(0, repo.resetCalls)
    }

    @Test
    fun `valid email sets resetSent and stops loading`() {
        val repo = StubAuthRepository()
        val vm = ForgotPasswordViewModel(repo)
        vm.onEmailChange("asha@example.com")

        vm.onSubmit()

        val state = vm.uiState.value
        assertTrue(state.resetSent)
        assertFalse(state.isLoading)
        assertNull(state.emailError)
        assertNull(state.formError)
        assertEquals(1, repo.resetCalls)
    }

    @Test
    fun `email is trimmed before reaching the repository`() {
        val repo = StubAuthRepository()
        val vm = ForgotPasswordViewModel(repo)
        vm.onEmailChange("  asha@example.com  ")

        vm.onSubmit()

        assertEquals("asha@example.com", repo.lastEmail)
    }

    @Test
    fun `unknown email still shows success so accounts are not revealed`() {
        val vm = ForgotPasswordViewModel(FakeAuthRepository(latencyMs = 0L))
        vm.onEmailChange("nobody@example.com")

        vm.onSubmit()

        assertTrue(vm.uiState.value.resetSent)
        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `network error shows form error and does not set resetSent`() {
        val fake = FakeAuthRepository(latencyMs = 0L).apply { simulateNetworkError = true }
        val vm = ForgotPasswordViewModel(fake)
        vm.onEmailChange("asha@example.com")

        vm.onSubmit()

        val state = vm.uiState.value
        assertEquals(AuthError.NETWORK, state.formError)
        assertFalse(state.resetSent)
        assertFalse(state.isLoading)
    }

    @Test
    fun `too many requests failure is passed through to form error`() {
        val repo = StubAuthRepository(resetResult = AuthResult.Failure(AuthError.TOO_MANY_REQUESTS))
        val vm = ForgotPasswordViewModel(repo)
        vm.onEmailChange("asha@example.com")

        vm.onSubmit()

        assertEquals(AuthError.TOO_MANY_REQUESTS, vm.uiState.value.formError)
        assertFalse(vm.uiState.value.resetSent)
    }

    @Test
    fun `editing the email clears field and form errors`() {
        val vm = ForgotPasswordViewModel(StubAuthRepository())
        vm.onSubmit() // blank -> field error
        assertEquals(FieldError.BLANK, vm.uiState.value.emailError)

        vm.onEmailChange("a")

        assertNull(vm.uiState.value.emailError)
        assertNull(vm.uiState.value.formError)
        assertEquals("a", vm.uiState.value.email)
    }

    @Test
    fun `second submit while loading is ignored`() {
        val gate = CompletableDeferred<Unit>()
        val repo = StubAuthRepository(gate = gate)
        val vm = ForgotPasswordViewModel(repo)
        vm.onEmailChange("asha@example.com")

        vm.onSubmit()
        assertTrue(vm.uiState.value.isLoading)
        vm.onSubmit() // ignored

        assertEquals(1, repo.resetCalls)

        gate.complete(Unit)

        assertTrue(vm.uiState.value.resetSent)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals(1, repo.resetCalls)
    }

    @Test
    fun `retry after a network failure succeeds`() {
        val repo = StubAuthRepository(resetResult = AuthResult.Failure(AuthError.NETWORK))
        val vm = ForgotPasswordViewModel(repo)
        vm.onEmailChange("asha@example.com")
        vm.onSubmit()
        assertEquals(AuthError.NETWORK, vm.uiState.value.formError)

        repo.resetResult = AuthResult.Success(Unit)
        vm.onSubmit()

        val state = vm.uiState.value
        assertTrue(state.resetSent)
        assertNull(state.formError)
        assertEquals(2, repo.resetCalls)
    }
}
