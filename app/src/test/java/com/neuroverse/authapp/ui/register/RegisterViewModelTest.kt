package com.neuroverse.authapp.ui.register

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
class RegisterViewModelTest {

    private val testUser = User(uid = "uid-1", name = "Asha", email = "asha@example.com")

    /** Records calls and returns a configurable result. Optionally waits on a gate. */
    private class StubAuthRepository(
        var registerResult: AuthResult<User>,
        private val gate: CompletableDeferred<Unit>? = null
    ) : AuthRepository {
        var registerCalls = 0
        var lastName: String? = null
        var lastEmail: String? = null
        var lastPassword: String? = null

        override suspend fun register(name: String, email: String, password: String): AuthResult<User> {
            registerCalls++
            lastName = name
            lastEmail = email
            lastPassword = password
            gate?.await()
            return registerResult
        }

        override suspend fun login(email: String, password: String): AuthResult<User> =
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

    private fun RegisterViewModel.fillValid() {
        onNameChange("  Asha  ")
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
        val state = RegisterViewModel(stub()).uiState.value

        assertEquals(RegisterUiState(), state)
        assertFalse(state.isLoading)
        assertFalse(state.registered)
    }

    @Test
    fun `editing a field updates its value and clears only its own error`() {
        val vm = RegisterViewModel(stub())
        vm.onSubmit() // all blank: three errors

        vm.onNameChange("A")

        val state = vm.uiState.value
        assertEquals("A", state.name)
        assertNull(state.nameError)
        assertEquals(FieldError.BLANK, state.emailError)
        assertEquals(FieldError.BLANK, state.passwordError)
    }

    @Test
    fun `editing a field clears the form error`() {
        val vm = RegisterViewModel(stub(AuthResult.Failure(AuthError.NETWORK)))
        vm.fillValid()
        vm.onSubmit()
        assertEquals(AuthError.NETWORK, vm.uiState.value.formError)

        vm.onNameChange("Asha K")

        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `toggle password visibility flips the flag`() {
        val vm = RegisterViewModel(stub())

        vm.onTogglePasswordVisibility()
        assertTrue(vm.uiState.value.isPasswordVisible)

        vm.onTogglePasswordVisibility()
        assertFalse(vm.uiState.value.isPasswordVisible)
    }

    // ---- input validation on submit ----

    @Test
    fun `submit with blank fields shows BLANK errors and does not call the repository`() {
        val repo = stub()
        val vm = RegisterViewModel(repo)

        vm.onSubmit()

        val state = vm.uiState.value
        assertEquals(FieldError.BLANK, state.nameError)
        assertEquals(FieldError.BLANK, state.emailError)
        assertEquals(FieldError.BLANK, state.passwordError)
        assertEquals(0, repo.registerCalls)
        assertFalse(state.isLoading)
    }

    @Test
    fun `submit with invalid email reports only the email error`() {
        val repo = stub()
        val vm = RegisterViewModel(repo)
        vm.fillValid()
        vm.onEmailChange("not-an-email")

        vm.onSubmit()

        val state = vm.uiState.value
        assertNull(state.nameError)
        assertEquals(FieldError.INVALID_EMAIL, state.emailError)
        assertNull(state.passwordError)
        assertEquals(0, repo.registerCalls)
    }

    @Test
    fun `submit with short password reports PASSWORD_TOO_SHORT`() {
        val repo = stub()
        val vm = RegisterViewModel(repo)
        vm.fillValid()
        vm.onPasswordChange("abcdefg")

        vm.onSubmit()

        assertEquals(FieldError.PASSWORD_TOO_SHORT, vm.uiState.value.passwordError)
        assertEquals(0, repo.registerCalls)
    }

    // ---- successful registration ----

    @Test
    fun `submit with valid input sends trimmed name and email and the raw password`() {
        val repo = stub()
        val vm = RegisterViewModel(repo)
        vm.fillValid()

        vm.onSubmit()

        assertEquals(1, repo.registerCalls)
        assertEquals("Asha", repo.lastName)
        assertEquals("asha@example.com", repo.lastEmail)
        assertEquals("password1", repo.lastPassword)
    }

    @Test
    fun `successful registration marks state as registered and not loading`() {
        val vm = RegisterViewModel(stub())
        vm.fillValid()

        vm.onSubmit()

        val state = vm.uiState.value
        assertTrue(state.registered)
        assertFalse(state.isLoading)
        assertNull(state.formError)
    }

    // ---- failures ----

    @Test
    fun `duplicate email failure shows EMAIL_ALREADY_IN_USE`() {
        val vm = RegisterViewModel(stub(AuthResult.Failure(AuthError.EMAIL_ALREADY_IN_USE)))
        vm.fillValid()

        vm.onSubmit()

        val state = vm.uiState.value
        assertEquals(AuthError.EMAIL_ALREADY_IN_USE, state.formError)
        assertFalse(state.registered)
        assertFalse(state.isLoading)
    }

    @Test
    fun `network failure shows NETWORK and allows a retry`() {
        val repo = stub(AuthResult.Failure(AuthError.NETWORK))
        val vm = RegisterViewModel(repo)
        vm.fillValid()
        vm.onSubmit()
        assertEquals(AuthError.NETWORK, vm.uiState.value.formError)
        assertFalse(vm.uiState.value.registered)

        repo.registerResult = AuthResult.Success(testUser)
        vm.onSubmit()

        assertTrue(vm.uiState.value.registered)
        assertNull(vm.uiState.value.formError)
        assertEquals(2, repo.registerCalls)
    }

    // ---- in-flight behaviour ----

    @Test
    fun `loading is true while the request is in flight and a second submit is ignored`() {
        val gate = CompletableDeferred<Unit>()
        val repo = stub(gate = gate)
        val vm = RegisterViewModel(repo)
        vm.fillValid()

        vm.onSubmit()
        assertTrue(vm.uiState.value.isLoading)

        vm.onSubmit() // ignored
        assertEquals(1, repo.registerCalls)

        gate.complete(Unit)
        assertFalse(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.registered)
    }

    // ---- with the real in-memory repository ----

    @Test
    fun `registering the same email twice with the in-memory repository reports a duplicate`() {
        val shared = FakeAuthRepository(latencyMs = 0L)

        val first = RegisterViewModel(shared)
        first.fillValid()
        first.onSubmit()
        assertTrue(first.uiState.value.registered)

        val second = RegisterViewModel(shared)
        second.onNameChange("Someone Else")
        second.onEmailChange("ASHA@example.com")
        second.onPasswordChange("password2")
        second.onSubmit()

        assertEquals(AuthError.EMAIL_ALREADY_IN_USE, second.uiState.value.formError)
        assertFalse(second.uiState.value.registered)
    }
}
