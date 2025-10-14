package com.example.ruto.ui.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*

/**
 * Date, Time Picker 관리를 위한 파일
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogM3(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
        title = { Text("시간 선택") },
        text = { TimePicker(state = state) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogM3(
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    val confirmEnabled by remember { derivedStateOf { state.selectedDateMillis != null } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let(onConfirm) },
                enabled = confirmEnabled
            ) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        title = { Text("날짜 선택") },
        text = { DatePicker(state = state) }
    )
}
