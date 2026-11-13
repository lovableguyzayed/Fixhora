package com.example

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.TaskFlowContainer
import com.example.ui.screens.auth.MobileLoginScreen
import com.example.ui.screens.auth.OtpVerificationScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.data.auth.AppUser
import com.example.data.auth.AuthError
import com.example.data.market.Offer
import com.example.data.market.Task
import com.example.ui.screens.account.AccountScreen
import com.example.ui.screens.auth.AuthUiState
import com.example.ui.screens.auth.NameScreen
import com.example.ui.screens.customer.CustomerHomeScreen
import com.example.ui.screens.customer.MyTasksScreen
import com.example.ui.screens.customer.TaskWithOffers
import com.example.ui.screens.helper.JobItem
import com.example.ui.screens.helper.MyJob
import com.example.ui.screens.helper.WorkerHomeScreen
import com.example.data.market.Message
import com.example.ui.screens.helper.MyJobsScreen
import com.example.ui.screens.shell.ChatThread
import com.example.ui.screens.shell.ChatsScreen
import com.example.ui.screens.taskflow.TaskLocationScreen
import com.example.ui.screens.taskflow.TaskPhotosScreen
import com.example.ui.screens.taskflow.TaskReviewScreen
import com.example.ui.screens.taskflow.TaskViewModel
import kotlinx.coroutines.flow.flowOf
import com.example.ui.theme.AppFrame
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders key screens to PNG (no phone needed) for the audit's before/after previews.
 * Run: ./gradlew testDebugUnitTest --tests com.example.ScreenshotTest
 * Output: app/build/screenshots/<size>/<screen>.png
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    private val now = System.currentTimeMillis()
    private fun task(id: Int, title: String, place: String, status: String = "submitted", min: String = "300", max: String = "800", cat: String = "repairs", ago: Long = 20) =
        Task(id = id.toString(), ownerId = "cust", ownerName = "Ramesh", status = status, categoryId = cat, locationQuery = place,
            title = title, details = "Water drips all night. Need a plumber today evening.",
            minBudget = min, maxBudget = max, createdAt = now - ago * 60_000)
    private fun offer(id: Int, task: Int, helper: String, name: String, amount: Long, msg: String = "", status: String = "sent") =
        Offer(id.toString(), task.toString(), helper, name, amount, msg, now, status)
    private val tap = task(1, "Kitchen tap is leaking", "Civil Lines, Kanpur")
    private val paint = task(2, "Paint one bedroom", "Vaishali Nagar, Jaipur", status = "accepted", min = "4000", max = "5000", cat = "painting", ago = 1500)
    private val clean = task(3, "Full house cleaning before Diwali", "Gomti Nagar, Lucknow", min = "1500", max = "2500", cat = "cleaning", ago = 60)
    private val myTasks = listOf(
        TaskWithOffers(tap, listOf(
            offer(1, 1, "h1", "Suresh Kumar", 650, "Can come today 6 pm"),
            offer(2, 1, "h2", "Anil", 500, "Tomorrow morning"),
            offer(3, 1, "h3", "Raju", 800))),
        TaskWithOffers(paint, listOf(offer(4, 2, "h4", "Raju (painter)", 4500, status = "accepted"))),
    )
    private val ramesh = AppUser("u1", "+919876543210", null, "Ramesh")

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "welcome" to { WelcomeScreen(false, null, {}, {}, {}) },
        "mobile_login" to { MobileLoginScreen(AuthUiState(), isTestMode = false, onSendOtp = { _, _ -> }, onBack = {}) },
        "otp" to { OtpVerificationScreen(AuthUiState(phone = "9876543210", verificationId = "x"), false, {}, {}, {}) },
        "otp_wrong" to { OtpVerificationScreen(AuthUiState(phone = "9876543210", verificationId = "x", error = AuthError.WrongCode, wrongTries = 1), false, {}, {}, {}) },
        "name" to { NameScreen(saving = false) {} },
        "role_selection" to { RoleSelectionScreen({}) },
        "task_category" to { TaskFlowContainer({}) },
        "customer_home" to { CustomerHomeScreen("Ramesh") {} },
        "my_tasks" to { MyTasksScreen(myTasks, {}, { _, _ -> }, {}, {}, {}) },
        "my_tasks_empty" to { MyTasksScreen(emptyList(), {}, { _, _ -> }, {}, {}, {}) },
        "helper_home" to {
            WorkerHomeScreen("Suresh", true,
                listOf(JobItem(tap, null, false), JobItem(clean, offer(9, 3, "me", "Suresh", 2000), false)),
                listOf(MyJob(offer(9, 3, "me", "Suresh", 2000), clean)), {}, { _, _, _ -> })
        },
        "account" to { AccountScreen(ramesh, false, false, null, false, {}, {}, {}, {}, {}) },
        "account_guest" to { AccountScreen(null, true, true, null, false, {}, {}, {}, {}, {}) },
        "task_location" to { TaskLocationScreen({}, filledTaskVm()) },
        "task_details" to { TaskPhotosScreen({}, filledTaskVm()) },
        "task_review" to { TaskReviewScreen({}, {}, filledTaskVm()) },
        "my_jobs" to {
            MyJobsScreen(listOf(
                MyJob(offer(9, 3, "me", "Suresh", 2000), clean),
                MyJob(offer(10, 2, "me", "Suresh", 4500, status = "accepted"), paint),
                MyJob(offer(11, 1, "me", "Suresh", 900, status = "declined"), tap.copy(status = "accepted")),
            ), {}, {})
        },
        "chats_list" to { ChatsScreen(threads, "customer", "", emptyList(), { flowOf(messages) }, { _, _ -> }) },
        "chat_conversation" to {
            ChatsScreen(threads, "customer", "", emptyList(), { flowOf(messages) }, { _, _ -> }, openTaskId = "2")
        },
        "chat_helper" to {
            ChatsScreen(threads, "worker", "", listOf("I can do this today.", "I'll arrive in 20 minutes."),
                { flowOf(messages) }, { _, _ -> }, openTaskId = "2")
        },
    )

    private val threads = listOf(ChatThread("2", "Paint one bedroom", "Raju (painter)", "₹4,500"))
    private val messages = listOf(
        Message("3", "2", "worker", "Theek hai, kal subah 10 baje aata hoon.", now - 2 * 60_000),
        Message("2", "2", "customer", "Kya aap kal subah aa sakte hain? Paint main le aaunga.", now - 10 * 60_000),
        Message("1", "2", "worker", "Namaste! Main Raju. 1 room ka kaam 1 din mein ho jayega.", now - 30 * 60_000),
    )

    /** Task form already filled in, like Ramesh ji would. */
    private fun filledTaskVm(): TaskViewModel {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<FixhoraApplication>()
        return TaskViewModel(app.taskRepository).apply {
            updateCategory("repairs")
            updateLocationQuery("12, Near Hanuman Mandir, Civil Lines, Kanpur 208001")
            updateDescription("Kitchen tap is leaking", "Water drips all night. Need a plumber today evening.")
            updateBudget("300", "800")
        }
    }

    private fun shoot(folder: String) {
        screens.forEach { (name, content) ->
            captureRoboImage("build/screenshots/$folder/$name.png") {
                AppFrame { content() }
            }
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xhdpi")
    fun phone360x800() = shoot("360x800")

    @Test
    @Config(qualifiers = "w320dp-h568dp-xhdpi")
    fun smallPhone320x568() = shoot("320x568")

    /** Shows the Hindi texts once values-hi exists (English until then). */
    @Test
    @Config(qualifiers = "hi-w360dp-h800dp-xhdpi")
    fun phoneHindi() = shoot("360x800_hi")

    @Test
    @Config(qualifiers = "w800dp-h1280dp-mdpi")
    fun tablet800x1280() = shoot("tablet800x1280")

    @Test
    @Config(qualifiers = "w360dp-h800dp-xhdpi")
    fun phoneMaxFont() {
        RuntimeEnvironment.setFontScale(2.0f)
        shoot("360x800_font200")
    }
}
