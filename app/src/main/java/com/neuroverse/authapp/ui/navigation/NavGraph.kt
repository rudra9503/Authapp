package com.neuroverse.authapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neuroverse.authapp.AppContainer
import com.neuroverse.authapp.ui.forgot.ForgotPasswordScreen
import com.neuroverse.authapp.ui.forgot.ForgotPasswordViewModel
import com.neuroverse.authapp.ui.login.LoginScreen
import com.neuroverse.authapp.ui.login.LoginViewModel
import com.neuroverse.authapp.ui.register.RegisterScreen
import com.neuroverse.authapp.ui.register.RegisterViewModel
import com.neuroverse.authapp.ui.screens.StubScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.WELCOME) {
        composable(Routes.WELCOME) {
            StubScreen(
                title = "Welcome",
                actions = listOf(
                    "Register" to { navController.navigate(Routes.REGISTER) },
                    "Login" to { navController.navigate(Routes.LOGIN) }
                )
            )
        }
        composable(Routes.REGISTER) {
            val viewModel: RegisterViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { RegisterViewModel(AppContainer.authRepository) }
                }
            )
            RegisterScreen(
                viewModel = viewModel,
                onRegistered = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onGoToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME)
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { LoginViewModel(AppContainer.authRepository) }
                }
            )
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onGoToRegister = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.WELCOME)
                    }
                }
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            val viewModel: ForgotPasswordViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ForgotPasswordViewModel(AppContainer.authRepository) }
                }
            )
            ForgotPasswordScreen(
                viewModel = viewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            StubScreen(
                title = "Home",
                actions = listOf(
                    "Profile" to { navController.navigate(Routes.PROFILE) },
                    "Logout (temporary)" to {
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            )
        }
        composable(Routes.PROFILE) {
            StubScreen(
                title = "Profile",
                actions = listOf("Back" to { navController.popBackStack() })
            )
        }
    }
}
