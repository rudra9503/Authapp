package com.neuroverse.authapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neuroverse.authapp.AppContainer
import com.neuroverse.authapp.ui.forgot.ForgotPasswordScreen
import com.neuroverse.authapp.ui.forgot.ForgotPasswordViewModel
import com.neuroverse.authapp.ui.home.HomeScreen
import com.neuroverse.authapp.ui.home.HomeViewModel
import com.neuroverse.authapp.ui.login.LoginScreen
import com.neuroverse.authapp.ui.login.LoginViewModel
import com.neuroverse.authapp.ui.profile.ProfileScreen
import com.neuroverse.authapp.ui.profile.ProfileViewModel
import com.neuroverse.authapp.ui.register.RegisterScreen
import com.neuroverse.authapp.ui.register.RegisterViewModel
import com.neuroverse.authapp.ui.welcome.WelcomeScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        startDestination = if (AppContainer.authRepository.currentUser() != null) {
            Routes.HOME
        } else {
            Routes.WELCOME
        }
    }
    val start = startDestination ?: return

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) }
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
                        popUpTo(navController.graph.id) { inclusive = true }
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
                        popUpTo(navController.graph.id) { inclusive = true }
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
            val viewModel: HomeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { HomeViewModel(AppContainer.authRepository) }
                }
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.PROFILE) {
            val viewModel: ProfileViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ProfileViewModel(AppContainer.authRepository) }
                }
            )
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
