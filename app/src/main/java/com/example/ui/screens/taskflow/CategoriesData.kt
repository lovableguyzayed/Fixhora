package com.example.ui.screens.taskflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.OrangeSecondary

data class Category(val id: String, val title: String, val subtitle: String, val icon: ImageVector, val iconTint: Color)

// A curated palette that sits well next to the brand blue / orange (no default Material hues).
val dummyCategories = listOf(
    Category("repairs", "Repairs", "Plumbing & electrical", Icons.Default.Build, BluePrimary),
    Category("cleaning", "Cleaning", "Home, office, deep cleaning", Icons.Default.CleaningServices, OrangeSecondary),
    Category("moving", "Moving", "Packing & transport", Icons.Default.LocalShipping, Color(0xFF16A34A)),
    Category("tech", "Tech Help", "Device setup, software help", Icons.Default.Computer, Color(0xFF7C3AED)),
    Category("errands", "Errands", "Groceries, pickups, delivery", Icons.Default.ShoppingCart, Color(0xFFE11D48)),
    Category("painting", "Painting", "Walls, touch-ups, polish", Icons.Default.FormatPaint, Color(0xFF0891B2)),
    Category("car", "Car Care", "Wash, battery, minor repair", Icons.Default.DirectionsCar, Color(0xFFD97706)),
    Category("tutoring", "Tutoring", "Academic help, online or home", Icons.Default.School, Color(0xFF4F46E5)),
    Category("more", "Other", "Anything not listed here", Icons.Default.MoreHoriz, Color(0xFF64748B))
)

fun categoryFor(id: String?): Category? = dummyCategories.find { it.id == id }
