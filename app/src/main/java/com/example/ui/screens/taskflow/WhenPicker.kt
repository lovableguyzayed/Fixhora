package com.example.ui.screens.taskflow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.room.TimeSlot
import com.example.ui.components.PillChip
import com.example.ui.components.SectionHeader
import com.example.ui.format.epochDayToUtcMillis
import com.example.ui.format.label
import com.example.ui.format.localTodayEpochDay
import com.example.ui.format.utcMillisToEpochDay
import com.example.ui.format.wantedDayText
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/**
 * "When do you need it?": a day — today, tomorrow, a date, or any day — and a part of that day.
 *
 * Tasks used to carry no time at all, so a helper could not tell a leak that needs fixing tonight
 * from a wall that can wait a fortnight. Both choices are optional; tapping a selected slot clears
 * it. Past days cannot be picked.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhenPicker(
  scheduledDate: Long?,
  timeSlot: TimeSlot?,
  onChange: (scheduledDate: Long?, timeSlot: TimeSlot?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val today = localTodayEpochDay()
  var picking by remember { mutableStateOf(false) }
  val pickedLater = scheduledDate != null && scheduledDate != today && scheduledDate != today + 1

  Column(modifier = modifier) {
    SectionHeader(title = stringResource(R.string.when_title))
    Spacer(Modifier.height(Spacing.xs))
    Text(
      stringResource(R.string.when_subtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = FixTheme.colors.textSecondary,
    )
    Spacer(Modifier.height(Spacing.md))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
      verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
      PillChip(stringResource(R.string.when_today), scheduledDate == today, { onChange(today, timeSlot) })
      PillChip(stringResource(R.string.when_tomorrow), scheduledDate == today + 1, { onChange(today + 1, timeSlot) })
      PillChip(
        text = if (pickedLater) wantedDayText(scheduledDate, today) else stringResource(R.string.when_pick_date),
        selected = pickedLater,
        onClick = { picking = true },
        icon = Icons.Default.CalendarMonth,
      )
      PillChip(stringResource(R.string.when_any_day), scheduledDate == null, { onChange(null, timeSlot) })
    }
    Spacer(Modifier.height(Spacing.md))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
      verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
      TimeSlot.entries.forEach { slot ->
        PillChip(
          text = slot.label(),
          selected = timeSlot == slot,
          onClick = { onChange(scheduledDate, if (timeSlot == slot) null else slot) },
        )
      }
    }
  }

  if (picking) {
    val state =
      rememberDatePickerState(
        initialSelectedDateMillis = epochDayToUtcMillis(scheduledDate?.coerceAtLeast(today) ?: (today + 2)),
        selectableDates =
          object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcMillisToEpochDay(utcTimeMillis) >= today
          },
      )
    DatePickerDialog(
      onDismissRequest = { picking = false },
      confirmButton = {
        TextButton(
          onClick = {
            state.selectedDateMillis?.let { onChange(utcMillisToEpochDay(it), timeSlot) }
            picking = false
          }
        ) {
          Text(stringResource(R.string.update_action_ok), color = FixTheme.colors.primary)
        }
      },
      dismissButton = {
        TextButton(onClick = { picking = false }) {
          Text(stringResource(R.string.update_action_cancel), color = FixTheme.colors.textSecondary)
        }
      },
      colors = DatePickerDefaults.colors(containerColor = FixTheme.colors.surface),
    ) {
      DatePicker(
        state = state,
        colors =
          DatePickerDefaults.colors(
            containerColor = FixTheme.colors.surface,
            selectedDayContainerColor = FixTheme.colors.highlight,
            selectedDayContentColor = FixTheme.colors.onHighlight,
            todayDateBorderColor = FixTheme.colors.primary,
            todayContentColor = FixTheme.colors.textPrimary,
          ),
      )
    }
  }
}
