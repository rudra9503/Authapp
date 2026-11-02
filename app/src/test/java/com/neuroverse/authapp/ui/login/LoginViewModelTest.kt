package com.neuroverse.authapp.ui.login

import com.neuroverse.authapp.data.repository.FakeAuthRepository
import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.model.User
import com.neuroverse.authapp.domain.repository.AuthRepository
import com.neuroverse.authapp.domain.validation.FieldError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
class LoginViewModelTest {

    private val testUser = User(uid = "uid-1", name = "Asha", email = "asha@example.com")

    /** Records calls and returns a configurable result. Optionally waits on a gate. */
    private class StubAuthRepository(
        var loginResult: AuthResult<User>,
        private val gate: CompletableDeferred<Unit>? = null
    ) : AuthRepository {
        var loginCalls = 0
        var lastEmail: String? = null
        var lastPassword: String? = null

        override suspend fun login(email: String, password: String): AuthResult<User> {
            loginCalls++
            lastEmail = email
            lastPassword = password
            gate?.await()
            return loginResult
        }

        override suspend fun register(name: String, email: String, password: String): AuthResult<User> =
            AuthResult.Failure(AuthError.UNKNOWN)

        override suspend fun sendPasswordReset(email: String): AuthResult<Unit> =
            AuthResult.Failure(AuthError.UNKNOWN)

        override suspend fun currentUser(): User? = null
        override suspend fun logout() {}
    }

    private fun stub(
        result: AuthResult<User> = AuthResult.Success(testUser),
        gate: CompletableDeferred<Unit>? = null
    ) = StubAuthRepository(result, gate)

    private fun LoginViewModel.fillValid() {
        onEmailChange("  asha@example.com ")
        onPasswordChange("password1")
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- state basics ----

    @Test
    fun `initial state is empty and idle`() {
        val state = LoginViewModel(stub()).uiState.value

        assertEquals(LoginUiState(), state)
        assertFalse(state.isLoading)
        assertFalse(state.loggedIn)
    }

    @Test
    fun `editing a field updates its value and clears only its own error`() {
        val vm = LoginViewModel(stub())
        vm.onSubmit() // both blank

        vm.onEmailChange("a")

        val state = vm.uiState.value
        assertEquals("a", state.email)
        assertNull(state.emailError)
        assertEquals(FieldError.BLANK, state.passwordError)
    }

    @Test
    fun `editing a field clears the form error`() {
        val vm = LoginViewModel(stub(AuthResult.Failure(AuthError.NETWORK)))
        vm.fillValid()
        vm.onSubmit()
        assertEquals(AuthError.NETWORK, vm.uiState.value.formError)

        vm.onPasswordChange("another")

        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `toggle password visibility flips the flag`() {
        val vm = LoginViewModel(stub())

        vm.onTogglePasswordVisibility()
        assertTrue(vm.uiState.value.isPasswordVisible)

        vm.onTogglePasswordVisibility()
        assertFalse(vm.uiState.value.isPasswordVisible)
    }

    // ---- input validation on submit ----

    @Test
    fun `submit with blank fields shows BLANK errors and does not call the repository`() {
        val repo = stub()
        val vm = LoginViewModel(repo)

        vm.onSubmit()

        val state = vm.uiState.value
        assertEquals(FieldError.BLANK, state.emailError)
        assertEquals(FieldError.BLANK, state.passwordError)
        assertEquals(0, repo.loginCalls)
        assertFalse(state.isLoading)
    }

    @Test
    fun `submit with invalid email format reports only the email error`() {
        val repo = stub()
        val vm = LoginViewModel(repo)
        vm.fillValid()
        vm.onEmailChange("not-an-email")

        vm.onSubmit()

        val state = vm.uiState.value
        assertEquals(FieldError.INVALID_EMAIL, state.emailError)
        assertNull(state.passwordError)
        assertEquals(0, repo.loginCalls)
    }

    @Test
    fun `submit accepts a short password because login does not enforce the policy`() {
        val repo = stub()
        val vm = LoginViewModel(repo)
        vm.onEmailChange("asha@example.com")
        vm.onPasswordChange("x")

        vm.onSubmit()

        assertNull(vm.uiState.value.passwordError)
        assertEquals(1, repo.loginCalls)
    }

    // ---- successful login ----

    @Test
    fun `submit with valid input sends trimmed email and the raw password`() {
        val repo = stub()
        val vm = LoginViewModel(repo)
        vm.fillValid()

        vm.onSubmit()

        assertEquals(1, repo.loginCalls)
        assertEquals("asha@example.com", repo.lastEmail)
        assertEquals("password1", repo.lastPassword)
    }

    @Test
    fun `successful login marks state as loggedIn and not loading`() {
        val vm = LoginViewModel(stub())
        vm.fillValid()

        vm.onSubmit()

        val state = vm.uiState.value
        assertTrue(state.loggedIn)
        assertFalse(state.isLoading)
        assertNull(state.formError)
    }

    // ---- failures ----

    @Test
    fun `wrong credentials show INVALID_CREDENTIALS and do not log in`() {
        val vm = LoginViewModel(stub(AuthResult.Failure(AuthError.INVALID_CREDENTIALS)))
        vm.fillValid()

        vm.onSubmit()

        val state = vm.uiState.value
        assertEquals(AuthError.INVALID_CREDENTIALS, state.formError)
        assertFalse(state.loggedIn)
        assertFalse(state.isLoading)
    }

    @Test
    fun `network failure shows NETWORK and allows a retry`() {
        val repo = stub(AuthResult.Failure(AuthError.NETWORK))
        val vm = LoginViewModel(repo)
        vm.fillValid()
        vm.onSubmit()
        assertEquals(AuthError.NETWORK, vm.uiState.value.formError)
        assertFalse(vm.uiState.value.loggedIn)

        repo.loginResult = AuthResult.Success(testUser)
        vm.onSubmit()

        assertTrue(vm.uiState.value.loggedIn)
        assertNull(vm.uiState.value.formError)
        assertEquals(2, repo.loginCalls)
    }

    // ---- in-flight behaviour ----

    @Test
    fun `loading is true while the request is in flight and a second submit is ignored`() {
        val gate = CompletableDeferred<Unit>()
        val repo = stub(gate = gate)
        val vm = LoginViewModel(repo)
        vm.fillValid()

        vm.onSubmit()
        assertTrue(vm.uiState.value.isLoading)

        vm.onSubmit() // ignored
        assertEquals(1, repo.loginCalls)

        gate.complete(Unit)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.loggedIn)
    }

    // ---- with the real in-memory repository ----

    @Test
    fun `login succeeds with the in-memory repository after registration`() {
        val shared = FakeAuthRepository(latencyMs = 0L)
        runBlocking {
            shared.register("Asha", "asha@example.com", "password1")
            shared.logout()
        }

        val vm = LoginViewModel(shared)
        vm.onEmailChange("  ASHA@Example.com ")
        vm.onPasswordChange("password1")
        vm.onSubmit()

        assertTrue(vm.uiState.value.loggedIn)
        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `wrong password and unknown email produce the same form error`() {
        val shared = FakeAuthRepository(latencyMs = 0L)
        runBlocking {
            shared.register("Asha", "asha@example.com", "password1")
            shared.logout()
        }

        val wrongPassword = LoginViewModel(shared).apply {
            onEmailChange("asha@example.com")
            onPasswordChange("wrong-password")
            onSubmit()
        }
        val unknownEmail = LoginViewModel(shared).apply {
            onEmailChange("nobody@example.com")
            onPasswordChange("password1")
            onSubmit()
        }

        assertEquals(AuthError.INVALID_CREDENTIALS, wrongPassword.uiState.value.formError)
        assertEquals(wrongPassword.uiState.value.formError, unknownEmail.uiState.value.formError)
        assertFalse(wrongPassword.uiState.value.loggedIn)
        assertFalse(unknownEmail.uiState.value.loggedIn)
    }
}
