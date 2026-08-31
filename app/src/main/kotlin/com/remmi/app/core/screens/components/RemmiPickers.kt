package com.remmi.app.core.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**                                 Date Picker Dialog
 * Shared Material 3 Date Picker interface
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    val timeZone = TimeZone.currentSystemDefault()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds()
    )


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val newDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
                    onDateSelected(newDate)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**                                 Date Range Picker Dialog
 * Shared Material 3 Date Range Picker interface
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiDateRangePickerDialog(
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    onDismiss: () -> Unit,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val timeZone = TimeZone.currentSystemDefault()
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds(),
        initialSelectedEndDateMillis = initialEndDate.atTime(0, 0).toInstant(timeZone).toEpochMilliseconds()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val start = dateRangePickerState.selectedStartDateMillis?.let {
                    Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
                }
                val end = dateRangePickerState.selectedEndDateMillis?.let {
                    Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date
                }
                if (start != null && end != null) {
                    onRangeSelected(start, end)
                } else if (start != null) {
                    onRangeSelected(start, start)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.height(500.dp)
        )
    }
}

/**                                 Time Picker Dialog
 * Shared Material 3 Time Picker interface (Clock)
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemmiTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = {
                        onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                        onDismiss()
                    }) { Text("OK") }
                }
            }
        }
    }
}

/**                                 Day Selection Dialog
 * Interface for selecting multiple days of the week
 * */
@Composable
fun RemmiDaySelectionDialog(
    selectedDays: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    val days = DayOfWeek.entries
    val currentSelected = remember { mutableStateListOf<String>().apply { addAll(selectedDays) } }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days") },
        text = {
            Column {
                days.forEach { day ->
                    val dayStr = day.name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (currentSelected.contains(dayStr)) currentSelected.remove(dayStr)
                                else currentSelected.add(dayStr)
                            }
                    ) {
                        Checkbox(
                            checked = currentSelected.contains(dayStr),
                            onCheckedChange = { checked ->
                                if (checked) currentSelected.add(dayStr)
                                else currentSelected.remove(dayStr)
                            }
                        )
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(currentSelected.toList()) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
