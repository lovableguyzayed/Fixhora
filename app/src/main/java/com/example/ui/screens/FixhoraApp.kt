package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.FixhoraApplication
import com.example.data.session.UserRole
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.CreateAccountScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.MobileLoginScreen
import com.example.ui.screens.auth.OtpVerificationScreen
import com.example.ui.screens.auth.PermissionRequestScreen
import com.example.ui.screens.auth.ProfileSetupScreen
import com.example.ui.screens.auth.SignInScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.helper.HelperFlowContainer
import com.example.ui.screens.helper.HelperViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
  object Splash : Screen("splash")

  object Welcome : Screen("welcome")

  object SignIn : Screen("sign_in")

  object MobileLogin : Screen("mobile_login")

  object ForgotPassword : Screen("forgot_password")

  object ProfileSetup : Screen("profile_setup")

  object PermissionRequest : Screen("permission_request")

  object RoleSelection : Screen("role_selection")

  object TaskFlow : Screen("task_flow")

  object HelperFlow : Screen("helper_flow")

  /** Carries the number whose code is being checked, so the screen can show it back. */
  object OtpVerification : Screen("otp_verification/{mobile}") {
    const val ARG_MOBILE = "mobile"

    fun createRoute(mobile: String) = "otp_verification/$mobile"
  }

  /** The mobile number is optional: it is pre-filled when arriving from a failed OTP sign-in. */
  object CreateAccount : Screen("create_account?mobile={mobile}") {
    const val ARG_MOBILE = "mobile"

    fun createRoute(mobile: String = "") = "create_account?mobile=$mobile"
  }
}

@Composable
fun FixhoraApp() {
  val navController = rememberNavController()
  val application = LocalContext.current.applicationContext as FixhoraApplication
  val scope = rememberCoroutineScope()

  NavHost(
    navController = navController,
    startDestination = Screen.Splash.route,
    modifier = Modifier.fillMaxSize(),
  ) {
    composable(Screen.Splash.route) {
      // Resolved once the session has actually been read, so a signed-in user is never bounced
      // through the welcome screen on every launch.
      var destination by remember { mutableStateOf<String?>(null) }
      LaunchedEffect(Unit) {
        val session = application.sessionManager.current()
        destination = if (session.isSignedIn) Screen.RoleSelection.route else Screen.Welcome.route
      }
      SplashScreen(
        onSplashComplete = {
          val target = destination ?: Screen.Welcome.route
          navController.navigateClearingBackStack(target)
        }
      )
    }

    composable(Screen.Welcome.route) {
      WelcomeScreen(
        onSignInClick = { navController.navigate(Screen.SignIn.route) },
        onCreateAccountClick = { navController.navigate(Screen.CreateAccount.createRoute()) },
        // Guests keep Welcome on the stack: backing out of role selection should return here,
        // where signing in is still offered.
        onGuestClick = { navController.navigate(Screen.RoleSelection.route) },
      )
    }

    composable(Screen.SignIn.route) {
      SignInScreen(
        viewModel = authViewModel(application),
        onLoginSuccess = { navController.navigateClearingBackStack(Screen.RoleSelection.route) },
        onMobileLoginClick = { navController.navigate(Screen.MobileLogin.route) },
        onCreateAccountClick = { navController.navigate(Screen.CreateAccount.createRoute()) },
        onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
      )
    }

    composable(Screen.MobileLogin.route) {
      MobileLoginScreen(
        onSendOtp = { mobile -> navController.navigate(Screen.OtpVerification.createRoute(mobile)) },
        onBack = { navController.popBackStack() },
      )
    }

    composable(
      route = Screen.OtpVerification.route,
      arguments = listOf(navArgument(Screen.OtpVerification.ARG_MOBILE) { type = NavType.StringType }),
    ) { backStackEntry ->
      val mobile = backStackEntry.arguments?.getString(Screen.OtpVerification.ARG_MOBILE).orEmpty()
      OtpVerificationScreen(
        viewModel = authViewModel(application),
        mobile = mobile,
        onVerifiedExistingAccount = {
          navController.navigateClearingBackStack(Screen.RoleSelection.route)
        },
        onNoAccountForMobile = { verifiedMobile ->
          navController.navigate(Screen.CreateAccount.createRoute(verifiedMobile)) {
            popUpTo(Screen.MobileLogin.route) { inclusive = true }
          }
        },
        onBack = { navController.popBackStack() },
      )
    }

    composable(
      route = Screen.CreateAccount.route,
      arguments =
        listOf(
          navArgument(Screen.CreateAccount.ARG_MOBILE) {
            type = NavType.StringType
            defaultValue = ""
          }
        ),
    ) { backStackEntry ->
      val prefilledMobile =
        backStackEntry.arguments?.getString(Screen.CreateAccount.ARG_MOBILE).orEmpty()
      CreateAccountScreen(
        viewModel = authViewModel(application),
        prefilledMobile = prefilledMobile,
        onCreateSuccess = { navController.navigate(Screen.ProfileSetup.route) },
        onMobileOtpClick = { navController.navigate(Screen.MobileLogin.route) },
        onBack = { navController.popBackStack() },
      )
    }

    composable(Screen.ForgotPassword.route) {
      ForgotPasswordScreen(
        viewModel = authViewModel(application),
        onPasswordReset = { navController.popBackStack() },
        onBack = { navController.popBackStack() },
      )
    }

    composable(Screen.ProfileSetup.route) {
      ProfileSetupScreen(
        viewModel = authViewModel(application),
        onProfileComplete = { navController.navigate(Screen.PermissionRequest.route) },
      )
    }

    composable(Screen.PermissionRequest.route) {
      PermissionRequestScreen(
        onPermissionsHandled = {
          navController.navigateClearingBackStack(Screen.RoleSelection.route)
        }
      )
    }

    composable(Screen.RoleSelection.route) {
      var signedInName by remember { mutableStateOf<String?>(null) }
      LaunchedEffect(Unit) {
        val userId = application.sessionManager.current().userId
        signedInName = userId?.let { application.userRepository.findById(it)?.fullName }
      }

      RoleSelectionScreen(
        signedInName = signedInName,
        onRoleSelected = { role ->
          scope.launch { application.sessionManager.setActiveRole(role) }
          when (role) {
            UserRole.CUSTOMER -> navController.navigate(Screen.TaskFlow.route)
            UserRole.HELPER -> navController.navigate(Screen.HelperFlow.route)
          }
        },
        onSignOut = {
          scope.launch {
            application.sessionManager.signOut()
            navController.navigateClearingBackStack(Screen.Welcome.route)
          }
        },
      )
    }

    composable(Screen.TaskFlow.route) {
      TaskFlowContainer(onBackToRoles = { navController.popBackStack() })
    }

    composable(Screen.HelperFlow.route) {
      val helperViewModel: HelperViewModel =
        viewModel(
          factory =
            HelperViewModel.Factory(
              application.taskRepository,
              application.chatRepository,
              application.userRepository,
            )
        )
      HelperFlowContainer(
        onBackToRoles = { navController.popBackStack() },
        viewModel = helperViewModel,
      )
    }
  }
}

@Composable
private fun authViewModel(application: FixhoraApplication): AuthViewModel =
  viewModel(factory = AuthViewModel.Factory(application.userRepository, application.sessionManager))

/**
 * Navigates and drops everything behind it.
 *
 * Used at every point where going "back" would return to a screen the user has finished with —
 * previously, pressing back after signing in landed on the sign-in form again.
 */
private fun NavHostController.navigateClearingBackStack(route: String) {
  navigate(route) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
  }
}
