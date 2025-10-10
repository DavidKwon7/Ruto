package com.example.ruto.ui.routine.edit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ruto.domain.routine.RoutineCadence

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineEditScreen(
    navController: NavHostController,
    vm: RoutineEditViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("루틴 편집") }) }
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
                    if (ui.notifyEnabled) {
                        OutlinedTextField(
                            value = ui.notifyTime.orEmpty(),
                            onValueChange = { vm.updateNotifyTime(it) },
                            label = { Text("알림 시각(HH:mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
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
