package com.neuroverse.authapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            StubScreen(
                title = "Register",
                actions = listOf("Go to Login" to { navController.navigate(Routes.LOGIN) })
            )
        }
        composable(Routes.LOGIN) {
            StubScreen(
                title = "Login",
                actions = listOf(
                    "Forgot password" to { navController.navigate(Routes.FORGOT_PASSWORD) },
                    "Home (temporary)" to {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                )
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            StubScreen(
                title = "Forgot Password",
                actions = listOf("Back" to { navController.popBackStack() })
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
