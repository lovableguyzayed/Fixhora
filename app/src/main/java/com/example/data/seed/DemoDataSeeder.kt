package com.example.data.seed

import com.example.data.room.TaskDao
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus

/**
 * Populates a handful of demo tasks so the helper feed is not empty on a fresh install.
 *
 * This exists only to make the app explorable during development. It is invoked from
 * `FixhoraApplication` behind `BuildConfig.DEBUG`, so a release build never writes demo rows —
 * previously 100 fake tasks were seeded into every build, including release.
 */
class DemoDataSeeder(private val taskDao: TaskDao) {

  /** No-op once any real or demo task exists, so the user's own posts are never joined by fakes. */
  suspend fun seedIfEmpty() {
    if (taskDao.countPostedTasks() > 0) return
    val now = System.currentTimeMillis()
    demoTasks(now).forEach { taskDao.insertTask(it) }
  }

  private fun demoTasks(now: Long): List<TaskEntity> {
    fun minutesAgo(minutes: Long) = now - minutes * 60_000L
    return listOf(
      demoTask(
        categoryId = "repairs",
        title = "Kitchen sink is leaking",
        details =
          "Water drips from the pipe under the sink whenever the tap runs. Need a plumber who can bring their own tools.",
        location = "Sector 62, Noida, Uttar Pradesh 201309",
        minBudget = "400",
        maxBudget = "900",
        distanceKm = 5,
        createdAt = minutesAgo(12),
      ),
      demoTask(
        categoryId = "cleaning",
        title = "Deep clean 2BHK before Diwali",
        details =
          "Full flat cleaning including kitchen chimney, bathrooms and balcony. Roughly 900 sq ft.",
        location = "Indiranagar, Bengaluru, Karnataka 560038",
        minBudget = "1500",
        maxBudget = "3000",
        distanceKm = 10,
        createdAt = minutesAgo(48),
      ),
      demoTask(
        categoryId = "moving",
        title = "Help shifting furniture to 3rd floor",
        details = "Two-seater sofa, a bed frame and six cartons. Building has no lift.",
        location = "Bandra West, Mumbai, Maharashtra 400050",
        minBudget = "800",
        maxBudget = "1600",
        distanceKm = 5,
        createdAt = minutesAgo(95),
      ),
      demoTask(
        categoryId = "tech",
        title = "Set up new printer with WiFi",
        details = "Bought an HP printer, need it connected to the home WiFi and to two laptops.",
        location = "Connaught Place, New Delhi, Delhi 110001",
        minBudget = "300",
        maxBudget = "600",
        distanceKm = 5,
        createdAt = minutesAgo(140),
      ),
      demoTask(
        categoryId = "painting",
        title = "Repaint one bedroom wall",
        details = "Seepage stains on one wall, about 10 x 9 ft. Putty and one coat of paint needed.",
        location = "Salt Lake, Kolkata, West Bengal 700091",
        minBudget = "1200",
        maxBudget = "2500",
        distanceKm = 10,
        createdAt = minutesAgo(210),
      ),
      demoTask(
        categoryId = "errands",
        title = "Pick up medicines from pharmacy",
        details = "Prescription is ready at the chemist near the metro station. Need it by evening.",
        location = "Sector 18, Noida, Uttar Pradesh 201301",
        minBudget = "150",
        maxBudget = "300",
        distanceKm = 5,
        createdAt = minutesAgo(260),
      ),
      demoTask(
        categoryId = "car",
        title = "Car battery died in the parking",
        details = "Need a jump start, and a check on whether the battery needs replacing.",
        location = "Koramangala, Bengaluru, Karnataka 560034",
        minBudget = "500",
        maxBudget = "1200",
        distanceKm = 5,
        createdAt = minutesAgo(320),
      ),
      demoTask(
        categoryId = "tutoring",
        title = "Class 9 maths tuition, twice a week",
        details = "Algebra and geometry. Prefer someone who can come home on weekday evenings.",
        location = "Aundh, Pune, Maharashtra 411007",
        minBudget = "2000",
        maxBudget = "4000",
        distanceKm = 10,
        createdAt = minutesAgo(400),
      ),
      demoTask(
        categoryId = "repairs",
        title = "Ceiling fan making noise",
        details = "Fan in the living room rattles at high speed. Might need bearing replacement.",
        location = "Gomti Nagar, Lucknow, Uttar Pradesh 226010",
        minBudget = "250",
        maxBudget = "700",
        distanceKm = 5,
        createdAt = minutesAgo(520),
      ),
      demoTask(
        categoryId = "cleaning",
        title = "Water tank cleaning",
        details = "500 litre overhead tank on the terrace, last cleaned over a year ago.",
        location = "Ameerpet, Hyderabad, Telangana 500016",
        minBudget = "600",
        maxBudget = "1100",
        distanceKm = 25,
        createdAt = minutesAgo(700),
      ),
    )
  }

  private fun demoTask(
    categoryId: String,
    title: String,
    details: String,
    location: String,
    minBudget: String,
    maxBudget: String,
    distanceKm: Int,
    createdAt: Long,
  ) =
    TaskEntity(
      status = TaskStatus.SUBMITTED,
      ownerId = DEMO_OWNER_ID,
      categoryId = categoryId,
      locationQuery = location,
      useCurrentLocation = false,
      selectedDistance = distanceKm,
      descriptionTitle = title,
      descriptionDetails = details,
      minBudget = minBudget,
      maxBudget = maxBudget,
      photoUris = "",
      createdAt = createdAt,
    )

  companion object {
    /** Distinguishes seeded rows from anything a real account posted. */
    const val DEMO_OWNER_ID = "demo"
  }
}
