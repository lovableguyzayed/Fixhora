package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.FixhoraApplication
import com.example.R
import com.example.data.auth.AppUser
import com.example.data.auth.AuthException
import com.example.ui.screens.account.AccountScreen
import com.example.ui.screens.auth.*
import com.example.ui.screens.customer.CustomerHomeScreen
import com.example.ui.screens.customer.CustomerViewModel
import com.example.ui.screens.customer.MyTasksScreen
import com.example.ui.screens.helper.HelperViewModel
import com.example.ui.screens.helper.MyJobsScreen
import com.example.ui.screens.helper.WorkerHomeScreen
import com.example.ui.screens.shell.*
import com.example.util.AppPrefs
import com.example.util.IndiaFormat
import kotlinx.coroutines.launch

object Routes {
    const val Splash = "splash"
    const val Welcome = "welcome"
    const val Mobile = "mobile"
    const val Otp = "otp"
    const val Name = "name"
    const val Role = "role"
    const val Customer = "customer"
    const val Helper = "helper"
    const val TaskFlow = "task_flow?category={category}"
    fun taskFlow(category: String? = null) = if (category == null) "task_flow" else "task_flow?category=$category"
}

@Composable
fun FixhoraApp() {
    val app = LocalContext.current.applicationContext as FixhoraApplication
    val prefs = remember { AppPrefs(app) }
    val auth = app.authRepository
    val user by auth.currentUser.collectAsState()
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.factory(auth))
    val authUi by authVm.ui.collectAsState()

    /** Clears everything behind [route] so Back never returns to login screens. */
    fun go(route: String) = nav.navigate(route) {
        popUpTo(nav.graph.id) { inclusive = true }
        launchSingleTop = true
    }
    fun homeRoute() = when (prefs.lastRole) { "helper" -> Routes.Helper; "customer" -> Routes.Customer; else -> Routes.Role }
    fun afterLogin(u: AppUser) {
        scope.launch { app.taskRepository.claimGuestTasks(u.uid) }
        go(if (u.name == null) Routes.Name else homeRoute())
    }
    LaunchedEffect(authUi.signedIn) {
        val u = user
        if (authUi.signedIn && u != null) { authVm.consumeSignedIn(); afterLogin(u) }
    }
    // Guests can browse; posting a task or sending an offer needs a login (decision D14).
    var loginPrompt by remember { mutableStateOf(false) }
    val requireLogin: (() -> Unit) -> Unit = { action -> if (auth.currentUser.value == null) loginPrompt = true else action() }
    LaunchedEffect(user) { if (user != null) loginPrompt = false }
    if (loginPrompt) {
        val activity = LocalContext.current.findActivity()
        LoginRequiredDialog(
            googleBusy = authUi.googleBusy,
            errorText = authErrorText(authUi.error),
            onMobile = { loginPrompt = false; authVm.clearError(); nav.navigate(Routes.Mobile) },
            onGoogle = { activity?.let { authVm.signInWithGoogle(it) } },
            onDismiss = { loginPrompt = false; authVm.clearError() },
        )
    }

    val signOutAndWipe: () -> Unit = {
        scope.launch {
            auth.signOut(); app.wipeLocalData(); prefs.clear()
            go(Routes.Welcome)
        }
    }

    NavHost(navController = nav, startDestination = Routes.Splash, modifier = Modifier.fillMaxSize()) {
        composable(Routes.Splash) {
            SplashScreen(onSplashComplete = {
                val u = auth.currentUser.value
                go(when { u == null -> Routes.Welcome; u.name == null -> Routes.Name; else -> homeRoute() })
            })
        }
        composable(Routes.Welcome) {
            val activity = LocalContext.current.findActivity()
            WelcomeScreen(
                googleBusy = authUi.googleBusy,
                errorText = authErrorText(authUi.error),
                onMobileClick = { authVm.clearError(); nav.navigate(Routes.Mobile) },
                onGoogleClick = { activity?.let { authVm.signInWithGoogle(it) } },
                onGuestClick = { authVm.clearError(); go(homeRoute()) },
            )
        }
        composable(Routes.Mobile) {
            MobileLoginScreen(
                ui = authUi, isTestMode = authVm.isTestMode,
                onSendOtp = { activity, phone -> authVm.sendOtp(activity, phone) { nav.navigate(Routes.Otp) { launchSingleTop = true } } },
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.Otp) {
            OtpVerificationScreen(
                ui = authUi, isTestMode = authVm.isTestMode,
                onVerify = authVm::verify,
                onResend = { authVm.sendOtp(it, authUi.phone, resend = true) },
                onChangeNumber = { authVm.clearError(); nav.popBackStack() },
                onEdited = { if (authUi.error != null) authVm.clearError() },
            )
        }
        composable(Routes.Name) {
            var saving by remember { mutableStateOf(false) }
            NameScreen(initialName = user?.name.orEmpty(), saving = saving, onSave = { name ->
                saving = true
                scope.launch {
                    auth.updateName(name).onSuccess { go(homeRoute()) }
                    saving = false
                }
            })
        }
        composable(Routes.Role) {
            RoleSelectionScreen(onRoleSelected = { role ->
                prefs.lastRole = if (role == "helper") "helper" else "customer"
                go(homeRoute())
            })
        }
        composable(Routes.TaskFlow, arguments = listOf(navArgument("category") { type = NavType.StringType; nullable = true })) { entry ->
            TaskFlowContainer(
                onBackToRoles = { nav.popBackStack() },
                ownerId = user?.uid ?: "guest",
                initialCategory = entry.arguments?.getString("category"),
                ownerName = user?.name,
            )
        }
        composable(Routes.Customer) {
            CustomerShell(app, user, nav, prefs, requireLogin, onSwitch = { prefs.lastRole = "helper"; go(Routes.Helper) },
                onLogin = { nav.navigate(Routes.Mobile) }, onSignOut = signOutAndWipe)
        }
        composable(Routes.Helper) {
            HelperShell(app, user, nav, prefs, requireLogin, onSwitch = { prefs.lastRole = "customer"; go(Routes.Customer) },
                onLogin = { nav.navigate(Routes.Mobile) }, onSignOut = signOutAndWipe)
        }
    }
}

private val customerTabs = listOf(
    ShellTab("c_home", R.string.nav_home, Icons.Default.Home),
    ShellTab("c_tasks", R.string.nav_my_tasks, Icons.AutoMirrored.Filled.Assignment),
    ShellTab("c_chats", R.string.nav_chats, Icons.AutoMirrored.Filled.Chat),
    ShellTab("account", R.string.nav_account, Icons.Default.Person),
)
private val helperTabs = listOf(
    ShellTab("h_home", R.string.nav_home, Icons.Default.Home),
    ShellTab("h_jobs", R.string.nav_my_jobs, Icons.AutoMirrored.Filled.Assignment),
    ShellTab("h_chats", R.string.nav_chats, Icons.AutoMirrored.Filled.Chat),
    ShellTab("account", R.string.nav_account, Icons.Default.Person),
)

@Composable
private fun CustomerShell(
    app: FixhoraApplication, user: AppUser?, rootNav: NavHostController, prefs: AppPrefs,
    requireLogin: (() -> Unit) -> Unit,
    onSwitch: () -> Unit, onLogin: () -> Unit, onSignOut: () -> Unit,
) {
    val postTask: (String?) -> Unit = { cat -> requireLogin { rootNav.navigate(Routes.taskFlow(cat)) } }
    val ownerId = user?.uid ?: "guest"
    val vm: CustomerViewModel = viewModel(key = "customer-$ownerId", factory = CustomerViewModel.Factory(app.market, ownerId))
    val tasks by vm.myTasks.collectAsState()
    val scope = rememberCoroutineScope()
    var openChat by remember { mutableStateOf<String?>(null) }
    ShellScaffold(customerTabs) { tabs ->
        composable("c_home") { CustomerHomeScreen(user?.name) { postTask(it) } }
        composable("c_tasks") {
            MyTasksScreen(
                tasks = tasks,
                onPostTask = { postTask(null) },
                onChoose = { t, o -> vm.chooseOffer(t, o) },
                onMarkDone = { vm.markDone(it.task) },
                onCancel = { vm.cancel(it.task) },
                onChat = { openChat = it.task.id; tabs.openTab("c_chats") },
            )
        }
        composable("c_chats") {
            val threads = tasks?.filter { it.chosen != null && it.task.status == "accepted" }?.map {
                ChatThread(it.task.id, it.task.title, it.chosen!!.helperName, IndiaFormat.rupees(it.chosen!!.amount))
            }
            ChatsScreen(
                threads = threads, me = "customer", emptyText = stringResource(R.string.chats_empty_customer),
                quickReplies = emptyList(), messagesFor = app.market::messages,
                onSend = { id, text -> scope.launch { app.market.sendMessage(id, "customer", text) } },
                openTaskId = openChat, onOpenConsumed = { openChat = null },
            )
        }
        composable("account") { AccountTab(app, user, isHelperSide = false, onSwitch, onLogin, onSignOut) }
    }
}

@Composable
private fun HelperShell(
    app: FixhoraApplication, user: AppUser?, rootNav: NavHostController, prefs: AppPrefs,
    requireLogin: (() -> Unit) -> Unit,
    onSwitch: () -> Unit, onLogin: () -> Unit, onSignOut: () -> Unit,
) {
    val helperId = user?.uid ?: "guest-helper"
    val helperName = user?.name ?: stringResource(R.string.helper_default_name)
    val vm: HelperViewModel = viewModel(key = "helper-$helperId",
        factory = HelperViewModel.Factory(app.market, app.taskRepository, prefs, helperId, helperName))
    val jobs by vm.jobs.collectAsState()
    val myJobs by vm.myJobs.collectAsState()
    val online by vm.online.collectAsState()
    val scope = rememberCoroutineScope()
    var openChat by remember { mutableStateOf<String?>(null) }
    val quickReplies = listOf("I can do this today.", "I'll arrive in 20 minutes.", "Work has been completed.")
    ShellScaffold(helperTabs) { tabs ->
        composable("h_home") {
            WorkerHomeScreen(helperName, online, jobs, myJobs, vm::setOnline, onSendOffer = { t, a, m -> requireLogin { vm.sendOffer(t, a, m) } })
        }
        composable("h_jobs") {
            MyJobsScreen(myJobs, onExploreJobs = { tabs.openTab("h_home") }, onChat = { openChat = it; tabs.openTab("h_chats") })
        }
        composable("h_chats") {
            val customer = stringResource(R.string.chat_customer)
            val threads = myJobs?.filter { it.offer.status == "accepted" && it.task?.status == "accepted" }?.map {
                ChatThread(it.offer.taskId, it.task?.title.orEmpty(), it.task?.ownerName?.takeIf { n -> n.isNotBlank() } ?: customer, IndiaFormat.rupees(it.offer.amount))
            }
            ChatsScreen(
                threads = threads, me = "worker", emptyText = stringResource(R.string.chats_empty_helper),
                quickReplies = quickReplies, messagesFor = app.market::messages,
                onSend = { id, text -> scope.launch { app.market.sendMessage(id, "worker", text) } },
                openTaskId = openChat, onOpenConsumed = { openChat = null },
            )
        }
        composable("account") { AccountTab(app, user, isHelperSide = true, onSwitch, onLogin, onSignOut) }
    }
}

@Composable
private fun AccountTab(
    app: FixhoraApplication, user: AppUser?, isHelperSide: Boolean,
    onSwitch: () -> Unit, onLogin: () -> Unit, onSignOut: () -> Unit,
) {
    val auth = app.authRepository
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<com.example.data.auth.AuthError?>(null) }
    AccountScreen(
        user = user, isHelperSide = isHelperSide, isTestMode = auth.isTestMode,
        errorText = authErrorText(error), busy = busy,
        onSwitchRole = onSwitch, onLogin = onLogin,
        onSaveName = { name -> scope.launch { auth.updateName(name) } },
        onLogout = onSignOut,
        onDeleteAccount = {
            busy = true; error = null
            scope.launch {
                // Remove what this person created on the server first (needs the login), then the login itself.
                user?.let { app.market.deleteDataOf(it.uid) }
                auth.deleteAccount()
                    .onSuccess { app.wipeLocalData(); onSignOut() }
                    .onFailure { error = (it as? AuthException)?.error }
                busy = false
            }
        },
    )
}
