package com.example.ui.screens.taskflow

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.BluePrimary

/**
 * One kind of job a customer can ask for.
 *
 * [id] is the storage value written into `TaskEntity.categoryId`; it must never change, or every
 * task already in the database loses its category. The words are resource ids rather than strings
 * precisely so the id and the label can move independently — the label is translated, the id is
 * not.
 */
data class Category(
  val id: String,
  @StringRes val titleRes: Int,
  @StringRes val subtitleRes: Int,
  val icon: ImageVector,
  val iconTint: Color,
)

/**
 * The category list, in the order it is offered.
 *
 * A top-level `val` so it is allocated once rather than rebuilt on every recomposition — that was
 * a real finding in the original audit.
 */
val dummyCategories =
  listOf(
    // Icon tints come from one curated set that sits beside the brand blue and orange, replacing
    // stock Material hues; the old amber (#FFC107) was barely visible on a white tile.
    Category("repairs", R.string.cat_repairs_title, R.string.cat_repairs_sub, Icons.Default.Build, BluePrimary),
    Category("cleaning", R.string.cat_cleaning_title, R.string.cat_cleaning_sub, Icons.Default.CleaningServices, Color(0xFFE85D00)),
    Category("moving", R.string.cat_moving_title, R.string.cat_moving_sub, Icons.Default.LocalShipping, Color(0xFF16A34A)),
    Category("tech", R.string.cat_tech_title, R.string.cat_tech_sub, Icons.Default.Computer, Color(0xFF7C3AED)),
    Category("errands", R.string.cat_errands_title, R.string.cat_errands_sub, Icons.Default.ShoppingCart, Color(0xFFE11D48)),
    Category("painting", R.string.cat_painting_title, R.string.cat_painting_sub, Icons.Default.FormatPaint, Color(0xFF0891B2)),
    Category("car", R.string.cat_car_title, R.string.cat_car_sub, Icons.Default.DirectionsCar, Color(0xFFD97706)),
    Category("tutoring", R.string.cat_tutoring_title, R.string.cat_tutoring_sub, Icons.Default.School, Color(0xFF4F46E5)),
    Category("more", R.string.cat_more_title, R.string.cat_more_sub, Icons.Default.MoreHoriz, Color(0xFF64748B)),
  )

/** Every category's title in the active language, keyed by id — what search matches against. */
@Composable
fun categoryTitles(): Map<String, String> =
  dummyCategories.associate { it.id to stringResource(it.titleRes) }
