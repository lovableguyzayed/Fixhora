package com.example.data.repository

import com.example.data.room.TaskDao
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TaskRepository(private val taskDao: TaskDao) {
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getTasksByStatus("submitted")
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    // Both view models call insertDummyData() on start; the lock keeps the seed from running twice.
    private val seedLock = Mutex()

    suspend fun insertDummyData() = seedLock.withLock {
        if (taskDao.getAllTasks().first().isNotEmpty()) return@withLock

        val now = System.currentTimeMillis()
        val minute = 60_000L
        sampleTasks.forEachIndexed { index, sample ->
            taskDao.insertTask(
                TaskEntity(
                    status = sample.status,
                    categoryId = sample.categoryId,
                    locationQuery = sample.location,
                    useCurrentLocation = false,
                    selectedDistance = listOf(2, 5, 10)[index % 3],
                    descriptionTitle = sample.title,
                    descriptionDetails = sample.details,
                    minBudget = sample.minBudget,
                    maxBudget = sample.maxBudget,
                    photoUris = "",
                    createdAt = now - sample.postedMinutesAgo * minute
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
        taskDao.insertTask(task.copy(status = "submitted", createdAt = System.currentTimeMillis()))
        // Delete the draft after submission
        val draft = taskDao.getDraftTask()
        if (draft != null) {
            taskDao.deleteTaskById(draft.id)
        }
    }

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        taskDao.updateTask(task.copy(status = newStatus))
    }

    private data class SampleTask(
        val categoryId: String,
        val title: String,
        val details: String,
        val location: String,
        val minBudget: String,
        val maxBudget: String,
        val postedMinutesAgo: Long,
        val status: String = "submitted",
    )

    private val sampleTasks = listOf(
        SampleTask("repairs", "Fix leaking kitchen tap", "The kitchen sink tap drips all day. Need someone with plumbing tools to replace the washer or cartridge.", "Sector 62, Noida", "300", "600", 12),
        SampleTask("cleaning", "Deep clean 2BHK apartment", "Full deep cleaning before a family visit: kitchen degreasing, two bathrooms, windows and floors.", "Indiranagar, Bengaluru", "1800", "2500", 28),
        SampleTask("tech", "Set up Wi-Fi router and smart TV", "New router and a 43\" smart TV need to be set up and connected. Should take about an hour.", "Andheri West, Mumbai", "500", "800", 45),
        SampleTask("moving", "Help shifting to a new flat", "Moving within the same area. Need two helpers to load and unload furniture and boxes. Tempo is arranged.", "Kothrud, Pune", "1500", "2200", 70),
        SampleTask("errands", "Pick up medicines from pharmacy", "Collect a prescription order from the pharmacy near the metro station and drop it home.", "Salt Lake, Kolkata", "150", "250", 95),
        SampleTask("painting", "Repaint living room walls", "One living room, about 400 sq ft. Paint is already bought; need prep, putty touch-ups and two coats.", "Banjara Hills, Hyderabad", "4000", "6000", 130),
        SampleTask("car", "Car wash and interior vacuum", "Hatchback needs an exterior wash, interior vacuum and dashboard polish at my building parking.", "Vasant Kunj, New Delhi", "400", "700", 180),
        SampleTask("tutoring", "Maths tutor for Class 10", "Looking for a home tutor, three evenings a week, to prepare for board exams (CBSE).", "Koramangala, Bengaluru", "3000", "5000", 240),
        SampleTask("repairs", "Install ceiling fan in bedroom", "Fan is already purchased. Need wiring and installation done safely; the ceiling hook is in place.", "Sector 18, Noida", "350", "500", 300),
        SampleTask("cleaning", "Sofa and carpet shampoo", "Five-seater fabric sofa and one 6x8 ft carpet need shampoo cleaning and drying.", "Powai, Mumbai", "900", "1400", 380),
        SampleTask("more", "Assemble office desk and chair", "Flat-pack desk and an ergonomic chair to assemble. All parts and tools included in the box.", "HSR Layout, Bengaluru", "400", "600", 460),
        SampleTask("errands", "Grocery shopping for parents", "Weekly grocery run for elderly parents; list will be shared on chat. Payment reimbursed on delivery.", "Kothrud, Pune", "200", "300", 540),
        SampleTask("tech", "Laptop running slow, needs clean-up", "Windows laptop takes ages to start. Need a clean-up, updates and antivirus set up.", "Sector 62, Noida", "600", "900", 640),
        SampleTask("painting", "Touch-up paint on balcony grill", "Rust spots on the balcony grill need scraping and a fresh coat of enamel paint.", "Andheri West, Mumbai", "800", "1200", 760),
        SampleTask("moving", "Pack and move study room", "Books, a study table and a bookshelf to be packed and moved to the floor above.", "Salt Lake, Kolkata", "700", "1000", 900),
        SampleTask("repairs", "Repair wardrobe door hinge", "One wardrobe door is hanging loose. Hinge replacement and alignment needed.", "Indiranagar, Bengaluru", "250", "450", 1500, status = "accepted"),
        SampleTask("cleaning", "Bathroom deep cleaning", "Two bathrooms need tile scrubbing, descaling of taps and a full clean.", "Banjara Hills, Hyderabad", "800", "1100", 1900, status = "accepted"),
        SampleTask("tutoring", "Spoken English practice", "Conversation practice for a working professional, two online sessions a week.", "Vasant Kunj, New Delhi", "2000", "3000", 2600, status = "accepted"),
        SampleTask("car", "Jump-start car battery", "Car will not start after a week of no use. Need a jump-start and a quick battery check.", "Sector 18, Noida", "300", "500", 4300, status = "completed"),
        SampleTask("more", "Water garden plants for a week", "Travelling for a week; need someone to water balcony plants every morning.", "Koramangala, Bengaluru", "700", "1000", 7200, status = "completed"),
    )
}
