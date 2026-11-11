package com.example.data.repository

import com.example.data.room.TaskDao
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(private val taskDao: TaskDao) {
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getTasksByStatus("submitted")
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    /**
     * Sample jobs for DEBUG builds only, so the helper screens can be tried without a server.
     * Runs once, only when there are no tasks at all. Never called in release builds.
     */
    suspend fun seedDemoTasksIfEmpty() {
        if (taskDao.getAllTasks().first().isNotEmpty()) return
        val samples = listOf(
            Triple("repairs", "Kitchen tap is leaking (sample)", "Near Hanuman Mandir, Civil Lines, Kanpur 208001"),
            Triple("cleaning", "Full house cleaning before Diwali (sample)", "Gomti Nagar, Lucknow 226010"),
            Triple("painting", "Paint one bedroom (sample)", "Vaishali Nagar, Jaipur 302021"),
            Triple("tech", "Set up new smart TV and Wi-Fi (sample)", "Sector 62, Noida 201309"),
            Triple("moving", "Shift 1BHK luggage to 2nd floor (sample)", "Kankarbagh, Patna 800020"),
            Triple("car", "Car battery jump-start (sample)", "Vijay Nagar, Indore 452010"),
        )
        samples.forEachIndexed { i, (cat, title, place) ->
            taskDao.insertTask(
                TaskEntity(
                    status = "submitted",
                    categoryId = cat,
                    locationQuery = place,
                    useCurrentLocation = false,
                    selectedDistance = 5,
                    descriptionTitle = title,
                    descriptionDetails = "Sample job shown only in test builds.",
                    minBudget = "${300 + i * 200}",
                    maxBudget = "${800 + i * 400}",
                    photoUris = ""
                )
            )
        }
    }

    suspend fun getDraftTask(): TaskEntity? = taskDao.getDraftTask()

    suspend fun saveDraft(task: TaskEntity) {
        val existingDraft = taskDao.getDraftTask()
        if (existingDraft != null) {
            taskDao.updateTask(task.copy(id = existingDraft.id))
        } else {
            taskDao.insertTask(task)
        }
    }

    suspend fun submitTask(task: TaskEntity) {
        // Remove the draft first, then store the posted task as a new row.
        taskDao.getDraftTask()?.let { taskDao.deleteTaskById(it.id) }
        taskDao.insertTask(task.copy(id = 0, status = "submitted"))
    }

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        taskDao.updateTask(task.copy(status = newStatus))
    }
}
