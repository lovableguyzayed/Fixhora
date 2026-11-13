package com.example.data.market

import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.room.OfferEntity
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Everything stays on this phone (Room database). Used until Firebase is set up. */
class LocalMarketRepository(
    private val tasks: TaskRepository,
    private val chats: ChatRepository,
) : MarketRepository {
    override val isShared = false

    override fun openJobs() = tasks.completedTasks.map { list -> list.map { it.toTask() } }
    override fun tasksOwnedBy(ownerId: String) = tasks.tasksOwnedBy(ownerId).map { l -> l.map { it.toTask() } }

    override fun offersOnTasksOf(ownerId: String): Flow<List<Offer>> =
        combine(tasks.tasksOwnedBy(ownerId), tasks.allOffers) { mine, offers ->
            val ids = mine.map { it.id }.toSet()
            offers.filter { it.taskId in ids }.map { it.toOffer() }
        }

    override fun offersSentBy(helperId: String): Flow<List<Pair<Offer, Task?>>> =
        combine(tasks.allTasks, tasks.allOffers) { all, offers ->
            offers.filter { it.helperId == helperId }.map { o -> o.toOffer() to all.firstOrNull { it.id == o.taskId }?.toTask() }
        }

    override suspend fun postTask(task: Task): Result<String> = runCatching {
        tasks.submitTask(task.toEntity()); ""
    }

    override suspend fun sendOffer(offer: Offer) = runCatching {
        tasks.sendOffer(OfferEntity(taskId = offer.taskId.toInt(), helperId = offer.helperId, helperName = offer.helperName,
            amount = offer.amount, message = offer.message))
    }

    override suspend fun chooseOffer(task: Task, chosen: Offer, allOffers: List<Offer>) = runCatching {
        tasks.acceptOffer(task.id.toInt(), chosen.id.toInt())
    }

    override suspend fun setStatus(task: Task, status: String) = runCatching {
        tasks.updateTaskStatus(task.toEntity(), status)
    }

    override fun messages(taskId: String) = chats.getMessagesForTask(taskId.toInt()).map { l ->
        l.map { Message(it.id.toString(), taskId, it.senderId, it.text, it.timestamp) }
    }

    override suspend fun sendMessage(taskId: String, senderRole: String, text: String) = runCatching {
        chats.sendMessage(taskId.toInt(), senderRole, text)
    }

    override suspend fun deleteDataOf(uid: String) { /* local data is wiped by FixhoraApplication.wipeLocalData */ }
}

fun TaskEntity.toTask() = Task(
    id = id.toString(), ownerId = ownerId, ownerName = null, status = status, categoryId = categoryId,
    locationQuery = locationQuery, title = descriptionTitle, details = descriptionDetails,
    minBudget = minBudget, maxBudget = maxBudget,
    photoUris = photoUris.split(",").filter { it.isNotBlank() }, createdAt = createdAt,
)

fun Task.toEntity() = TaskEntity(
    id = id.toIntOrNull() ?: 0, status = status, categoryId = categoryId, locationQuery = locationQuery,
    useCurrentLocation = false, selectedDistance = 5, descriptionTitle = title, descriptionDetails = details,
    minBudget = minBudget, maxBudget = maxBudget, photoUris = photoUris.joinToString(","), ownerId = ownerId, createdAt = createdAt,
)

fun OfferEntity.toOffer() = Offer(id.toString(), taskId.toString(), helperId, helperName, amount, message, createdAt, status)
