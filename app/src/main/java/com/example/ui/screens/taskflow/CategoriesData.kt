package com.example.ui.screens.taskflow

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.BluePrimary

/** Category names come from strings.xml so they can be shown in Hindi. */
data class Category(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
    val iconTint: Color,
)

val Category.title: String @Composable get() = stringResource(titleRes)
val Category.subtitle: String @Composable get() = stringResource(subtitleRes)

val dummyCategories = listOf(
    Category("repairs", R.string.category_repairs, R.string.category_repairs_sub, Icons.Default.Build, BluePrimary),
    Category("cleaning", R.string.category_cleaning, R.string.category_cleaning_sub, Icons.Default.CleaningServices, Color(0xFFFF8C00)),
    Category("moving", R.string.category_moving, R.string.category_moving_sub, Icons.Default.LocalShipping, Color(0xFF4CAF50)),
    Category("tech", R.string.category_tech, R.string.category_tech_sub, Icons.Default.Computer, Color(0xFF9C27B0)),
    Category("errands", R.string.category_errands, R.string.category_errands_sub, Icons.Default.ShoppingCart, Color(0xFFF44336)),
    Category("painting", R.string.category_painting, R.string.category_painting_sub, Icons.Default.FormatPaint, Color(0xFF00BCD4)),
    Category("car", R.string.category_car, R.string.category_car_sub, Icons.Default.DirectionsCar, Color(0xFFFFC107)),
    Category("tutoring", R.string.category_tutoring, R.string.category_tutoring_sub, Icons.Default.School, Color(0xFF3F51B5)),
    Category("more", R.string.category_more, R.string.category_more_sub, Icons.Default.MoreHoriz, Color(0xFF795548))
)
