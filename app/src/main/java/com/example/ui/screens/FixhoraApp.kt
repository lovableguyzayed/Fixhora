package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object SignIn : Screen("sign_in")
    object MobileLogin : Screen("mobile_login")
    object OtpVerification : Screen("otp_verification")
    object CreateAccount : Screen("create_account")
    object ForgotPassword : Screen("forgot_password")
    object ProfileSetup : Screen("profile_setup")
    object PermissionRequest : Screen("permission_request")
    object RoleSelection : Screen("role_selection")
    object TaskFlow : Screen("task_flow")
    object HelperFlow : Screen("helper_flow")
}

@Composable
fun FixhoraApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = Modifier.fillMaxSize()) {
        composable(Screen.Splash.route) {
            com.example.ui.screens.auth.SplashScreen(
                onSplashComplete = { navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                } }
            )
        }
        composable(Screen.Welcome.route) {
            com.example.ui.screens.auth.WelcomeScreen(
                onSignInClick = { navController.navigate(Screen.SignIn.route) },
                onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                onGuestClick = { navController.navigate(Screen.RoleSelection.route) }
            )
        }
        composable(Screen.SignIn.route) {
            com.example.ui.screens.auth.SignInScreen(
                onLoginSuccess = { navController.navigate(Screen.RoleSelection.route) },
                onMobileLoginClick = { navController.navigate(Screen.MobileLogin.route) },
                onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(Screen.MobileLogin.route) {
            com.example.ui.screens.auth.MobileLoginScreen(
                onSendOtp = { navController.navigate(Screen.OtpVerification.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OtpVerification.route) {
            com.example.ui.screens.auth.OtpVerificationScreen(
                onVerified = { navController.navigate(Screen.ProfileSetup.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CreateAccount.route) {
            com.example.ui.screens.auth.CreateAccountScreen(
                onCreateSuccess = { navController.navigate(Screen.ProfileSetup.route) },
                onMobileOtpClick = { navController.navigate(Screen.MobileLogin.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ForgotPassword.route) {
            com.example.ui.screens.auth.ForgotPasswordScreen(
                onPasswordReset = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ProfileSetup.route) {
            com.example.ui.screens.auth.ProfileSetupScreen(
                onProfileComplete = { navController.navigate(Screen.PermissionRequest.route) }
            )
        }
        composable(Screen.PermissionRequest.route) {
            com.example.ui.screens.auth.PermissionRequestScreen(
                onPermissionsHandled = { navController.navigate(Screen.RoleSelection.route) {
                    popUpTo(0) { inclusive = true }
                } }
            )
        }
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    if (role == "user") {
                        navController.navigate(Screen.TaskFlow.route)
                    } else if (role == "helper") {
                        navController.navigate(Screen.HelperFlow.route)
                    }
                }
            )
        }
        composable(Screen.TaskFlow.route) {
            TaskFlowContainer(
                onBackToRoles = { navController.popBackStack() }
            )
        }
        composable(Screen.HelperFlow.route) {
            val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.example.FixhoraApplication
            val viewModel: com.example.ui.screens.helper.HelperViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.example.ui.screens.helper.HelperViewModel.Factory(application.taskRepository, application.chatRepository)
            )
            com.example.ui.screens.helper.HelperFlowContainer(
                onBackToRoles = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
