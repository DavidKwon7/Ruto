package com.handylab.ruto.ui.routine

import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.ui.event.UiEvent
import com.handylab.ruto.ui.util.DatePickerDialogM3
import com.handylab.ruto.ui.util.TimePickerDialogM3
import com.handylab.ruto.ui.util.pad2
import com.handylab.ruto.ui.util.parseHHmm
import com.handylab.ruto.util.millisToLocalDate
import com.handylab.ruto.util.toEpochMillis
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineCreateScreen(
    navController: NavHostController,
    vm: RoutineCreateViewModel = hiltViewModel(),
    onSaved: (String) -> Unit = {}
) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.savedId) {
        uiState.savedId?.let {
            onSaved(it)
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        vm.uiEvent.collectLatest { e ->
            if (e is UiEvent.ShowToastMsg)
                Toast.makeText(
                    context, e.message, Toast.LENGTH_SHORT
                ).show()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("루틴 등록") }) }) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(
                    Modifier
                        .padding(24.dp)
                        .align(Alignment.Center)
                )
            } else {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = vm::updateName,
                        label = { Text("루틴 명칭") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            value = uiState.cadence.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("주기") }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            RoutineCadence.entries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = { vm.updateCadence(c); expanded = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    RoutineDateSelectPicker(ui = uiState, vm = vm)
                    Spacer(Modifier.height(12.dp))

                    // 알림
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("알림 사용")
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = uiState.notifyEnabled, onCheckedChange = vm::toggleNotify)
                    }
                    if (uiState.notifyEnabled) {
                        val (initHour, initMinute) = remember(uiState.notifyTime) {
                            parseHHmm(uiState.notifyTime)
                        }
                        var showPicker by remember { mutableStateOf(false) }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.notifyTime.orEmpty(),
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

                    // TODO 태그 (프로젝트의 Chip/선택 UI로 대체)
                    /*Text("태그: ${ui.tags.joinToString()}")
                    Row {
                        // Button(onClick = { vm.updateTags((ui.tags + "건강").distinct()) }) { Text("#건강") }
                        Button(
                            onClick = {
                                vm.updateTags((ui.tags + RoutineTag.Fixed(FixedTag.HEALTH)).distinct())
                            }
                        ) { Text("#건강") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                vm.updateTags((ui.tags + RoutineTag.Fixed(FixedTag.SELF_IMPROVEMENT)).distinct())
                            }
                        ) { Text("#자기개발") }

                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                vm.updateTags((ui.tags + RoutineTag.Fixed(FixedTag.HOUSEHOLD)).distinct())
                            }
                        ) { Text("#집안일") }
                    }

                    var custom by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it },
                        label = { Text("커스텀 태그") }
                    )
                    Button(
                        onClick = {
                            val t = custom.trim()
                            if (t.isNotEmpty()) {
                                vm.updateTags((ui.tags + RoutineTag.Custom(t)).distinct())
                                custom = ""
                            }
                        }
                    ) { Text("추가") }*/

                    Spacer(Modifier.height(20.dp))

                    Button(
                        enabled = !uiState.loading,
                        onClick = vm::submit,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (uiState.loading) "저장 중..." else "저장") }

                    uiState.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineDateSelectPicker(
    ui: RoutineFormState,
    vm: RoutineCreateViewModel
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startInitialMillis = remember(ui.startDate) { ui.startDate.toEpochMillis() }
    val endInitialMillis = remember(ui.endDate) { ui.endDate.toEpochMillis() }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = ui.startDate.toString(),
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
            value = ui.endDate.toString(),
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
                vm.updateStartDate(millisToLocalDate(millis))
                showStartPicker = false
            }
        )
    }
    if (showEndPicker) {
        DatePickerDialogM3(
            initialDateMillis = endInitialMillis,
            onDismiss = { showEndPicker = false },
            onConfirm = { millis ->
                vm.updateEndDate(millisToLocalDate(millis))
                showEndPicker = false
            }
        )
    }
}