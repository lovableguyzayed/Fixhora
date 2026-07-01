package com.example.ui.screens.taskflow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.BluePrimary

data class Category(val id: String, val title: String, val subtitle: String, val icon: ImageVector, val iconTint: Color)

val dummyCategories = listOf(
    Category("repairs", "Home Repairs", "Plumbing, electrical, carpentry & more", Icons.Default.Build, BluePrimary),
    Category("cleaning", "Cleaning", "Home, office, deep cleaning", Icons.Default.CleaningServices, Color(0xFFFF8C00)),
    Category("moving", "Moving Help", "Packing, loading, transport & more", Icons.Default.LocalShipping, Color(0xFF4CAF50)),
    Category("tech", "Tech Support", "Device setup, software help", Icons.Default.Computer, Color(0xFF9C27B0)),
    Category("errands", "Errands", "Grocery, pickup, delivery & more", Icons.Default.ShoppingCart, Color(0xFFF44336)),
    Category("painting", "Painting", "Home painting, touch ups & more", Icons.Default.FormatPaint, Color(0xFF00BCD4)),
    Category("car", "Car Services", "Wash, minor repair, battery & more", Icons.Default.DirectionsCar, Color(0xFFFFC107)),
    Category("tutoring", "Tutoring", "Academic help, online or in-person", Icons.Default.School, Color(0xFF3F51B5)),
    Category("more", "More", "Others not listed here", Icons.Default.MoreHoriz, Color(0xFF795548))
)
