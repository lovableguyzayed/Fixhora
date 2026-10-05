package com.example.ui.components

import com.example.data.room.TaskEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val indianNumberFormat: NumberFormat = NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN"))

/** "₹1,250" for a numeric string, or the raw text if it is not a number. */
fun formatRupees(amount: String): String {
    val value = amount.trim().toLongOrNull() ?: return "₹${amount.trim()}"
    return "₹${indianNumberFormat.format(value)}"
}

/** Human readable budget range, or null when no budget was entered. */
fun formatBudget(min: String, max: String): String? = when {
    min.isNotBlank() && max.isNotBlank() -> "${formatRupees(min)} – ${formatRupees(max)}"
    min.isNotBlank() -> "From ${formatRupees(min)}"
    max.isNotBlank() -> "Up to ${formatRupees(max)}"
    else -> null
}

fun TaskEntity.budgetLabel(): String = formatBudget(minBudget, maxBudget) ?: "Open to offers"

fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = ((now - timestamp) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

fun clockTime(timestamp: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

fun greetingForNow(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

/**
 * Stable placeholder people data for the demo marketplace. Tasks do not store a customer
 * yet, so names, ratings and distances are derived from the task id instead of showing
 * "Customer Name" style placeholders.
 */
object MockPeople {
    const val helperName = "Rahul Verma"

    private val customerNames = listOf(
        "Priya Sharma", "Amit Patel", "Neha Gupta", "Rohan Mehta", "Ananya Iyer",
        "Vikram Singh", "Sneha Reddy", "Arjun Nair", "Kavya Joshi", "Siddharth Rao"
    )

    fun customerName(taskId: Int): String = customerNames[(taskId.coerceAtLeast(0)) % customerNames.size]

    fun rating(taskId: Int): String = "%.1f".format(Locale.US, 4.3 + (taskId % 7) / 10.0)

    fun distanceKm(taskId: Int): String = "%.1f km".format(Locale.US, 0.6 + (taskId * 37 % 64) / 10.0)

    fun openingMessage(task: TaskEntity): String =
        "Hi! Are you available for \"${task.descriptionTitle.ifBlank { "my task" }}\"?"
}

/** Short neighbourhood label for a task, e.g. "Sector 62, Noida". */
fun TaskEntity.areaLabel(): String {
    val full = if (useCurrentLocation || locationQuery.isBlank()) "Sector 62, Noida, Uttar Pradesh" else locationQuery
    return full.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(2).joinToString(", ")
}

/** "Today", "Yesterday" or a short date, for chat day separators. */
fun dayLabel(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val day = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance().apply { timeInMillis = now }
    val sameYear = day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
    val dayDiff = today.get(Calendar.DAY_OF_YEAR) - day.get(Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dayDiff == 0 -> "Today"
        sameYear && dayDiff == 1 -> "Yesterday"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
