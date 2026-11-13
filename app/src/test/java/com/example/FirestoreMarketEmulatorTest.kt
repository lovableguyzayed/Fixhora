package com.example

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.market.FirestoreMarketRepository
import com.example.data.market.Offer
import com.example.data.market.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper

/**
 * End-to-end test of the shared (Firestore) marketplace against Google's local emulators,
 * with the real security rules. Two separate logged-in users: a customer and a helper.
 * Runs only when the emulators are up:
 *   cd firebase/rules-tests && npx firebase emulators:exec --only firestore,auth --project demo-fixorax \
 *     "cd ../.. && ./gradlew testDebugUnitTest --tests com.example.FirestoreMarketEmulatorTest"
 */
@RunWith(AndroidJUnit4::class)
class FirestoreMarketEmulatorTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Before fun onlyWithEmulator() = assumeTrue("Firestore emulator not running", System.getenv("FIRESTORE_EMULATOR_HOST") != null)
    @After fun tearDown() { scope.cancel(); FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).forEach { it.delete() } }

    private fun user(name: String): Pair<FirestoreMarketRepository, String> {
        val app = FirebaseApp.initializeApp(
            ApplicationProvider.getApplicationContext(),
            FirebaseOptions.Builder().setProjectId("demo-fixorax").setApplicationId("1:1:android:1").setApiKey("fake-key").build(),
            name,
        )
        val auth = FirebaseAuth.getInstance(app).apply { useEmulator("127.0.0.1", 9099) }
        val db = FirebaseFirestore.getInstance(app).apply {
            useEmulator("127.0.0.1", 8080)
            firestoreSettings = FirebaseFirestoreSettings.Builder().setLocalCacheSettings(memoryCacheSettings {}).build()
        }
        val uid = block { auth.signInAnonymously() }.user!!.uid
        return FirestoreMarketRepository(db, auth) to uid
    }

    /** Firebase calls back on the main thread, which Robolectric only runs when we let it. */
    private fun <T> block(start: () -> com.google.android.gms.tasks.Task<T>): T {
        val t = start()
        val until = System.currentTimeMillis() + 20_000
        while (!t.isComplete && System.currentTimeMillis() < until) { ShadowLooper.idleMainLooper(); Thread.sleep(20) }
        assertTrue("timed out", t.isComplete)
        return t.result
    }

    private fun <T> waitFor(flow: Flow<T>, what: String, ok: (T) -> Boolean): T {
        val latest = MutableStateFlow<Any?>(null)
        val job = scope.launch { flow.collect { latest.value = it } }
        val until = System.currentTimeMillis() + 20_000
        try {
            while (System.currentTimeMillis() < until) {
                ShadowLooper.idleMainLooper()
                @Suppress("UNCHECKED_CAST") val v = latest.value as T?
                if (v != null && ok(v)) return v
                Thread.sleep(25)
            }
            throw AssertionError("timed out waiting for: $what (last value: ${latest.value})")
        } finally { job.cancel() }
    }

    private fun run(block: suspend () -> Unit) {
        val job = scope.launch { block() }
        while (job.isActive) { ShadowLooper.idleMainLooper(); Thread.sleep(20) }
    }

    @Test fun customerAndHelperOnDifferentPhones() {
        val (customer, custUid) = user("customer")
        val (helper, helpUid) = user("helper")

        run { customer.postTask(Task("", custUid, "Ramesh", "submitted", "repairs", "Civil Lines, Kanpur", "Kitchen tap is leaking", "", "300", "800")).getOrThrow() }
        val job = waitFor(helper.openJobs(), "helper sees the task") { it.any { t -> t.title == "Kitchen tap is leaking" } }.single()
        assertEquals("800", job.maxBudget)

        run { helper.sendOffer(Offer("", job.id, helpUid, "Suresh", 650, "Can come at 6 pm")).getOrThrow() }
        val offers = waitFor(customer.offersOnTasksOf(custUid), "customer sees the offer") { it.size == 1 }
        assertEquals(650L, offers.single().amount)
        waitFor(helper.offersSentBy(helpUid), "helper sees own offer waiting") { it.singleOrNull()?.first?.status == "sent" }

        val myTask = waitFor(customer.tasksOwnedBy(custUid), "customer task") { it.size == 1 }.single()
        run { customer.chooseOffer(myTask, offers.single(), offers).getOrThrow() }
        waitFor(helper.offersSentBy(helpUid), "helper is chosen") { it.singleOrNull()?.first?.status == "accepted" && it.single().second?.status == "accepted" }
        waitFor(helper.openJobs(), "task no longer open") { it.isEmpty() }

        run { customer.sendMessage(myTask.id, "customer", "Namaste").getOrThrow() }
        run { helper.sendMessage(myTask.id, "worker", "6 baje aata hoon").getOrThrow() }
        waitFor(customer.messages(myTask.id), "chat both ways") { it.map { m -> m.text }.toSet() == setOf("Namaste", "6 baje aata hoon") }

        run { customer.deleteDataOf(custUid) }
        waitFor(customer.tasksOwnedBy(custUid), "customer data deleted") { it.isEmpty() }
    }
}
