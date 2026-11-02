package com.neuroverse.authapp.ui.home

import com.neuroverse.authapp.domain.model.AuthError
import com.neuroverse.authapp.domain.model.AuthResult
import com.neuroverse.authapp.domain.model.User
import com.neuroverse.authapp.domain.repository.AuthRepository
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
class HomeViewModelTest {

    private val testUser = User(uid = "uid-1", name = "Asha", email = "asha@example.com")

    private class StubAuthRepository(
        var user: User?,
        private val logoutGate: CompletableDeferred<Unit>? = null
    ) : AuthRepository {
        var logoutCalls = 0

        override suspend fun currentUser(): User? = user

        override suspend fun logout() {
            logoutCalls++
            logoutGate?.await()
            user = null
        }

        override suspend fun register(name: String, email: String, password: String): AuthResult<User> =
            AuthResult.Failure(AuthError.UNKNOWN)

        override suspend fun login(email: String, password: String): AuthResult<User> =
            AuthResult.Failure(AuthError.UNKNOWN)

        override suspend fun sendPasswordReset(email: String): AuthResult<Unit> =
            AuthResult.Failure(AuthError.UNKNOWN)
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
    fun `signed-in user is loaded and screen stays visible`() {
        val vm = HomeViewModel(StubAuthRepository(testUser))

        val state = vm.uiState.value
        assertEquals(testUser, state.user)
        assertFalse(state.isLoading)
        assertFalse(state.loggedOut)
    }

    @Test
    fun `no signed-in user marks loggedOut so the screen redirects`() {
        val vm = HomeViewModel(StubAuthRepository(null))

        val state = vm.uiState.value
        assertNull(state.user)
        assertFalse(state.isLoading)
        assertTrue(state.loggedOut)
    }

    @Test
    fun `logout calls the repository and clears the user`() {
        val repo = StubAuthRepository(testUser)
        val vm = HomeViewModel(repo)

        vm.onLogout()

        val state = vm.uiState.value
        assertEquals(1, repo.logoutCalls)
        assertNull(state.user)
        assertTrue(state.loggedOut)
        assertFalse(state.isLoading)
    }

    @Test
    fun `logout clears the repository session`() {
        val repo = StubAuthRepository(testUser)
        val vm = HomeViewModel(repo)

        vm.onLogout()

        assertNull(repo.user)
    }

    @Test
    fun `second logout while in progress is ignored`() {
        val gate = CompletableDeferred<Unit>()
        val repo = StubAuthRepository(testUser, gate)
        val vm = HomeViewModel(repo)

        vm.onLogout()
        assertTrue(vm.uiState.value.isLoading)
        vm.onLogout() // ignored

        assertEquals(1, repo.logoutCalls)

        gate.complete(Unit)

        assertTrue(vm.uiState.value.loggedOut)
        assertEquals(1, repo.logoutCalls)
    }

    @Test
    fun `logout after already logged out does nothing`() {
        val repo = StubAuthRepository(null)
        val vm = HomeViewModel(repo)

        vm.onLogout()

        assertEquals(0, repo.logoutCalls)
        assertTrue(vm.uiState.value.loggedOut)
    }
}
