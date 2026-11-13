package com.example.data.market

import kotlinx.coroutines.flow.Flow

/** A customer's task, as the screens see it (same shape on this phone and on the server). */
data class Task(
    val id: String,
    val ownerId: String?,
    val ownerName: String?,
    val status: String, // submitted, accepted, completed, cancelled
    val categoryId: String?,
    val locationQuery: String,
    val title: String,
    val details: String,
    val minBudget: String,
    val maxBudget: String,
    val photoUris: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val chosenHelperId: String? = null,
)

data class Offer(
    val id: String,
    val taskId: String,
    val helperId: String,
    val helperName: String,
    val amount: Long,
    val message: String = "",
    val createdAt: Long = 0L,
    val status: String = "sent", // sent, accepted, declined
)

data class Message(val id: String, val taskId: String, val senderRole: String, val text: String, val timestamp: Long)

/**
 * Tasks, offers and chats. Two versions:
 *  - [LocalMarketRepository]: stored only on this phone (test mode, before Firebase is set up);
 *  - [FirestoreMarketRepository]: shared between phones through Cloud Firestore.
 */
interface MarketRepository {
    /** True when data is shared between phones (Firestore). */
    val isShared: Boolean

    fun openJobs(): Flow<List<Task>>
    fun tasksOwnedBy(ownerId: String): Flow<List<Task>>
    /** Offers on all tasks owned by [ownerId]. */
    fun offersOnTasksOf(ownerId: String): Flow<List<Offer>>
    /** Offers sent by [helperId], each with its task (null if the task was deleted). */
    fun offersSentBy(helperId: String): Flow<List<Pair<Offer, Task?>>>

    suspend fun postTask(task: Task): Result<String>
    /** Returns false if this helper already sent an offer on the task. */
    suspend fun sendOffer(offer: Offer): Result<Boolean>
    suspend fun chooseOffer(task: Task, chosen: Offer, allOffers: List<Offer>): Result<Unit>
    suspend fun setStatus(task: Task, status: String): Result<Unit>

    fun messages(taskId: String): Flow<List<Message>>
    suspend fun sendMessage(taskId: String, senderRole: String, text: String): Result<Unit>

    /** Account deletion: remove everything this person created. */
    suspend fun deleteDataOf(uid: String)
}
