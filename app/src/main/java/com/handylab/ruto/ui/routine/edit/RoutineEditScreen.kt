package com.handylab.ruto.ui.routine.edit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.ui.util.DatePickerDialogM3
import com.handylab.ruto.ui.util.TimePickerDialogM3
import com.handylab.ruto.ui.util.pad2
import com.handylab.ruto.ui.util.parseHHmm
import com.handylab.ruto.util.epochMillisToYYYYMMDD
import com.handylab.ruto.util.parseYYYYMMDD
import com.handylab.ruto.util.toEpochMillis

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineEditScreen(
    navController: NavHostController,
    vm: RoutineEditViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    // 저장 성공 시 뒤로
    LaunchedEffect(ui.saved) {
        if (ui.saved) navController.popBackStack()
    }

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
                CircularProgressIndicator(
                    Modifier
                    .padding(24.dp)
                    .align(Alignment.Center)
                )
            } else {
                Column(Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth().padding(16.dp)) {
                    OutlinedTextField(
                        value = ui.name,
                        onValueChange = vm::updateName,
                        label = { Text("루틴 명칭") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    // 간단한 Cadence 선택 Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            value = ui.cadence.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("주기") }
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            RoutineCadence.entries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = { vm.updateCadence(c); expanded = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    var showStartPicker by remember { mutableStateOf(false) }
                    var showEndPicker by remember { mutableStateOf(false) }

                    val startInitialMillis = remember(ui.startDate) { parseYYYYMMDD(ui.startDate)?.toEpochMillis() }
                    val endInitialMillis = remember(ui.endDate) { parseYYYYMMDD(ui.endDate)?.toEpochMillis() }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ui.startDate,
                            onValueChange = {}, // 수동 입력 막기
                            readOnly = true,
                            label = { Text("시작일(YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { showStartPicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "시작일 선택")
                                }
                            }
                        )
                        OutlinedTextField(
                            value = ui.endDate,
                            onValueChange = {}, // 수동 입력 막기
                            readOnly = true,
                            label = { Text("종료일(YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { showEndPicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "종료일 선택")
                                }
                            }
                        )
                    }

                    if (showStartPicker) {
                        DatePickerDialogM3(
                            initialDateMillis = startInitialMillis,
                            onDismiss = { showStartPicker = false },
                            onConfirm = { millis ->
                                vm.updateStartDate(epochMillisToYYYYMMDD(millis))
                                showStartPicker = false
                            }
                        )
                    }
                    if (showEndPicker) {
                        DatePickerDialogM3(
                            initialDateMillis = endInitialMillis,
                            onDismiss = { showEndPicker = false },
                            onConfirm = { millis ->
                                vm.updateEndDate(epochMillisToYYYYMMDD(millis))
                                showEndPicker = false
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = ui.notifyEnabled, onCheckedChange = vm::updateNotifyEnabled)
                        Text("알림 사용")
                    }
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

                    // TODO 태그 기능 구현하기
                    /*var tagsText by remember(ui.tags) { mutableStateOf(ui.tags.joinToString(", ")) }
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = {
                            tagsText = it
                            vm.updateTags(it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() })
                        },
                        label = { Text("태그(콤마로 구분)") },
                        modifier = Modifier.fillMaxWidth()
                    )*/

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
                }
            }
        }
    }
}