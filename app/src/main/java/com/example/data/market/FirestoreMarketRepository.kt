package com.example.data.market

import com.google.android.gms.tasks.Task as GmsTask
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Tasks, offers and chats shared between phones through Cloud Firestore.
 * Data layout and who may read/write what: firebase/firestore.rules (tested in firebase/rules-tests).
 * Firestore keeps an offline copy: lists still show on a weak network, and every write is saved on the
 * phone at once and sent to the server when the network returns. That is why writes are not awaited —
 * waiting for the server would leave buttons spinning forever on a bad connection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreMarketRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : MarketRepository {
    override val isShared = true

    private val tasksCol get() = db.collection("tasks")
    private fun offersCol(taskId: String) = tasksCol.document(taskId).collection("offers")
    private fun messagesCol(taskId: String) = tasksCol.document(taskId).collection("messages")
    private fun myOfferIndex(uid: String) = db.collection("users").document(uid).collection("offers")
    private fun uid() = auth.currentUser?.uid ?: error("Not logged in")

    // ---------------- reading (live) ----------------

    override fun openJobs(): Flow<List<Task>> =
        tasksCol.whereEqualTo("status", "submitted").limit(200).listen { snap ->
            snap.documents.mapNotNull { it.toTask() }.sortedByDescending { it.createdAt }
        }

    override fun tasksOwnedBy(ownerId: String): Flow<List<Task>> =
        tasksCol.whereEqualTo("ownerId", ownerId).listen { snap ->
            snap.documents.mapNotNull { it.toTask() }.sortedByDescending { it.createdAt }
        }

    override fun offersOnTasksOf(ownerId: String): Flow<List<Offer>> =
        tasksOwnedBy(ownerId).flatMapLatest { mine ->
            if (mine.isEmpty()) flowOf(emptyList())
            else combine(mine.map { t -> offersCol(t.id).listen { s -> s.documents.mapNotNull { it.toOffer(t.id) } } }) { it.toList().flatten() }
        }

    override fun offersSentBy(helperId: String): Flow<List<Pair<Offer, Task?>>> =
        myOfferIndex(helperId).listen { s -> s.documents.map { it.id } }.flatMapLatest { taskIds ->
            if (taskIds.isEmpty()) flowOf(emptyList())
            else combine(taskIds.map { id ->
                combine(offersCol(id).document(helperId).listenDoc { it.toOffer(id) }, tasksCol.document(id).listenDoc { it.toTask() }) { o, t -> o?.let { it to t } }
            }) { pairs -> pairs.filterNotNull().sortedByDescending { it.first.createdAt } }
        }

    override fun messages(taskId: String): Flow<List<Message>> =
        messagesCol(taskId).orderBy("createdAt", Query.Direction.DESCENDING).limit(300).listen { s ->
            s.documents.map { d ->
                Message(d.id, taskId, d.getString("senderRole") ?: "", d.getString("text") ?: "",
                    d.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis())
            }
        }

    // ---------------- writing ----------------

    override suspend fun postTask(task: Task): Result<String> = runCatching {
        val ref = tasksCol.document()
        ref.set(
            mapOf(
                "ownerId" to uid(), "ownerName" to (task.ownerName ?: "").take(40), "status" to "submitted",
                "categoryId" to (task.categoryId ?: "more"), "locationQuery" to task.locationQuery.trim(),
                "title" to task.title.trim(), "details" to task.details.trim(),
                "minBudget" to task.minBudget.toLongOrNull(), "maxBudget" to task.maxBudget.toLongOrNull(),
                "createdAt" to FieldValue.serverTimestamp(), "chosenHelperId" to null, "chosenAmount" to null,
            )
        ).logFailure("postTask")
        ref.id
    }

    override suspend fun sendOffer(offer: Offer): Result<Boolean> = runCatching {
        val me = uid()
        db.batch().apply {
            set(offersCol(offer.taskId).document(me), mapOf(
                "helperId" to me, "helperName" to offer.helperName.take(40), "amount" to offer.amount,
                "message" to offer.message.take(200), "createdAt" to FieldValue.serverTimestamp(), "status" to "sent",
            ))
            set(myOfferIndex(me).document(offer.taskId), mapOf("taskId" to offer.taskId))
        }.commit().logFailure("sendOffer") // the rules refuse a second offer from the same helper
        true
    }

    override suspend fun chooseOffer(task: Task, chosen: Offer, allOffers: List<Offer>): Result<Unit> = runCatching {
        db.batch().apply {
            allOffers.filter { it.status == "sent" || it.id == chosen.id }.forEach { o ->
                update(offersCol(task.id).document(o.id), "status", if (o.id == chosen.id) "accepted" else "declined")
            }
            update(tasksCol.document(task.id), mapOf("status" to "accepted", "chosenHelperId" to chosen.helperId, "chosenAmount" to chosen.amount))
        }.commit().logFailure("chooseOffer")
        Unit
    }

    override suspend fun setStatus(task: Task, status: String): Result<Unit> = runCatching {
        tasksCol.document(task.id).update("status", status).logFailure("setStatus")
        Unit
    }

    override suspend fun sendMessage(taskId: String, senderRole: String, text: String): Result<Unit> = runCatching {
        messagesCol(taskId).add(mapOf(
            "senderUid" to uid(), "senderRole" to senderRole, "text" to text.take(1000), "createdAt" to FieldValue.serverTimestamp(),
        )).logFailure("sendMessage")
        Unit
    }

    override suspend fun deleteDataOf(uid: String) {
        // Offers this person sent (+ their own messages in those chats).
        runCatching {
            myOfferIndex(uid).get().await().documents.forEach { idx ->
                val taskId = idx.id
                runCatching {
                    messagesCol(taskId).whereEqualTo("senderUid", uid).get().await().documents.forEach { it.reference.delete().await() }
                }
                runCatching { offersCol(taskId).document(uid).delete().await() }
                runCatching { idx.reference.delete().await() }
            }
        }
        // Tasks this person posted, with their offers and chats.
        runCatching {
            tasksCol.whereEqualTo("ownerId", uid).get().await().documents.forEach { t ->
                runCatching { messagesCol(t.id).get().await().documents.forEach { it.reference.delete().await() } }
                runCatching { offersCol(t.id).get().await().documents.forEach { it.reference.delete().await() } }
                runCatching { t.reference.delete().await() }
            }
        }
    }

    // ---------------- mapping ----------------

    private fun DocumentSnapshot.toTask(): Task? {
        if (!exists()) return null
        fun budget(key: String) = (get(key) as? Number)?.toLong()?.toString() ?: ""
        return Task(
            id = id, ownerId = getString("ownerId"), ownerName = getString("ownerName"),
            status = getString("status") ?: "submitted", categoryId = getString("categoryId"),
            locationQuery = getString("locationQuery") ?: "", title = getString("title") ?: "",
            details = getString("details") ?: "", minBudget = budget("minBudget"), maxBudget = budget("maxBudget"),
            createdAt = getTimestamp("createdAt").millisOrNow(), chosenHelperId = getString("chosenHelperId"),
        )
    }

    private fun DocumentSnapshot.toOffer(taskId: String): Offer? {
        if (!exists()) return null
        return Offer(
            id = id, taskId = taskId, helperId = getString("helperId") ?: id, helperName = getString("helperName") ?: "",
            amount = (get("amount") as? Number)?.toLong() ?: 0L, message = getString("message") ?: "",
            createdAt = getTimestamp("createdAt").millisOrNow(), status = getString("status") ?: "sent",
        )
    }

    // The server time is filled in a moment after writing; show "now" meanwhile.
    private fun Timestamp?.millisOrNow() = this?.toDate()?.time ?: System.currentTimeMillis()
}

/** Live query as a Flow. On an error (e.g. no permission) it shows an empty list instead of hanging. */
private fun <T> Query.listen(map: (com.google.firebase.firestore.QuerySnapshot) -> List<T>): Flow<List<T>> = callbackFlow {
    val reg = addSnapshotListener { snap, err ->
        if (snap != null) trySend(map(snap)) else if (err != null) trySend(emptyList())
    }
    awaitClose { reg.remove() }
}

private fun <T> com.google.firebase.firestore.DocumentReference.listenDoc(map: (DocumentSnapshot) -> T?): Flow<T?> = callbackFlow {
    val reg = addSnapshotListener { snap, err ->
        if (snap != null) trySend(map(snap)) else if (err != null) trySend(null)
    }
    awaitClose { reg.remove() }
}

private fun <T> GmsTask<T>.logFailure(what: String): GmsTask<T> = addOnFailureListener {
    if (com.example.BuildConfig.DEBUG) android.util.Log.w("FirestoreMarket", "$what failed", it)
}

private suspend fun <T> GmsTask<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { t ->
        if (t.isSuccessful) cont.resume(t.result)
        else cont.resumeWithException(t.exception ?: IllegalStateException("Firestore task failed"))
    }
}
