package com.example.data.repository

import com.example.data.room.TaskDao
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(private val taskDao: TaskDao) {
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getTasksByStatus("submitted")
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertDummyData() {
        val existingTasks = taskDao.getAllTasks().first()
        if (existingTasks.size < 50) {
            val categories = listOf("repairs", "cleaning", "moving", "tech", "errands", "painting", "car", "tutoring", "more")
            val locations = listOf("New York, NY", "Los Angeles, CA", "Chicago, IL", "Houston, TX", "Phoenix, AZ")
            for (i in 1..100) {
                val cat = categories[i % categories.size]
                val task = TaskEntity(
                    status = "submitted",
                    categoryId = cat,
                    locationQuery = locations[i % locations.size],
                    useCurrentLocation = i % 2 == 0,
                    selectedDistance = (i % 50) + 5,
                    descriptionTitle = "Urgent $cat needed",
                    descriptionDetails = "This is a detailed description for test task $i. I need some help with $cat right away.",
                    minBudget = "${(i % 10) * 10 + 20}",
                    maxBudget = "${(i % 10) * 10 + 100}",
                    photoUris = ""
                )
                taskDao.insertTask(task)
            }
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
        taskDao.insertTask(task.copy(status = "submitted"))
        // Delete the draft after submission
        val draft = taskDao.getDraftTask()
        if (draft != null) {
            taskDao.deleteTaskById(draft.id)
        }
    }

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        taskDao.updateTask(task.copy(status = newStatus))
    }
}
