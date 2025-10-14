package com.example.ruto.ui.routine.edit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ruto.domain.routine.RoutineCadence
import com.example.ruto.ui.util.TimePickerDialogM3

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineEditScreen(
    navController: NavHostController,
    vm: RoutineEditViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("루틴 편집") },
            actions = {
                IconButton(
                    onClick = { vm.delete { navController.popBackStack() } },
                    enabled = !ui.saving
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제")
                }
            }
        ) },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            if (ui.loading) {
                CircularProgressIndicator(Modifier.padding(24.dp))
            } else {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    OutlinedTextField(
                        value = ui.name,
                        onValueChange = vm::updateName,
                        label = { Text("루틴 명칭") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    // 간단한 Cadence 선택 (실서비스에선 Dropdown 등 권장)
                    CadenceRow(ui.cadence) { vm.updateCadence(it) }
                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ui.startDate,
                            onValueChange = vm::updateStartDate,
                            label = { Text("시작일(YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ui.endDate,
                            onValueChange = vm::updateEndDate,
                            label = { Text("종료일(YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = ui.notifyEnabled, onCheckedChange = vm::updateNotifyEnabled)
                        Text("알림 사용")
                    }
                    /*if (ui.notifyEnabled) {
                        OutlinedTextField(
                            value = ui.notifyTime.orEmpty(),
                            onValueChange = { vm.updateNotifyTime(it) },
                            label = { Text("알림 시각(HH:mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }*/
                    if (ui.notifyEnabled) {
                        val (initHour, initMinute) = remember(ui.notifyTime) {
                            parseHHmm(ui.notifyTime)
                        }
                        var showPicker by remember { mutableStateOf(false) }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = ui.notifyTime.orEmpty(),
                                onValueChange = {}, // 사용자가 직접 수정하지 않음
                                readOnly = true,
                                label = { Text("알림 시각(HH:mm)") },
                                modifier = Modifier.weight(1f),
                                trailingIcon = {
                                    IconButton(onClick = { showPicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "시간 선택"
                                        )
                                    }
                                }
                            )
                            Button(onClick = { showPicker = true }) { Text("시간 선택") }
                        }

                        if (showPicker) {
                            TimePickerDialogM3(
                                initialHour = initHour,
                                initialMinute = initMinute,
                                onDismiss = { showPicker = false },
                                onConfirm = { h, m ->
                                    vm.updateNotifyTime("${pad2(h)}:${pad2(m)}")
                                    showPicker = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 태그 간단 편집(콤마 구분)
                    var tagsText by remember(ui.tags) { mutableStateOf(ui.tags.joinToString(", ")) }
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = {
                            tagsText = it
                            vm.updateTags(it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() })
                        },
                        label = { Text("태그(콤마로 구분)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (ui.error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(ui.error ?: "", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        enabled = !ui.saving,
                        onClick = {
                            vm.save()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (ui.saving) CircularProgressIndicator(Modifier.size(18.dp))
                        else Text("저장")
                    }

                    // 저장 성공 시 뒤로
                    LaunchedEffect(ui.saved) {
                        if (ui.saved) navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
private fun CadenceRow(selected: RoutineCadence, onSelect: (RoutineCadence) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RoutineCadence.values().forEach { c ->
            FilterChip(
                selected = (c == selected),
                onClick = { onSelect(c) },
                label = { Text(c.name) }
            )
        }
    }
}

private fun parseHHmm(hhmm: String?): Pair<Int, Int> {
    return try {
        if (hhmm.isNullOrBlank()) {
            val now = java.time.LocalTime.now()
            now.hour to now.minute
        } else {
            val parts = hhmm.split(":")
            parts[0].toInt() to parts[1].toInt()
        }
    } catch (_: Exception) {
        val now = java.time.LocalTime.now()
        now.hour to now.minute
    }
}

private fun pad2(n: Int) = n.toString().padStart(2, '0')
