package com.example.ruto.ui.routine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ruto.domain.routine.FixedTag
import com.example.ruto.domain.routine.RoutineCadence
import com.example.ruto.domain.routine.RoutineTag
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCreateScreen(
    navController: NavHostController,
    vm: RoutineCreateViewModel = hiltViewModel(),
    onSaved: (String) -> Unit = {}
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(ui.savedId) {
        ui.savedId?.let { onSaved(it) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("루틴 등록") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(
                value = ui.name,
                onValueChange = vm::updateName,
                label = { Text("루틴 명칭") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // Cadence
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
                    RoutineCadence.values().forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = { vm.updateCadence(c); expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 기간 (프로젝트의 DatePicker로 대체)
            Text("기간: ${ui.startDate} ~ ${ui.endDate}")
            Row {
                Button(onClick = { vm.updateStartDate(LocalDate.now()) }) { Text("시작=오늘") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.updateEndDate(LocalDate.now().plusMonths(1)) }) { Text("끝=+1M") }
            }
            Spacer(Modifier.height(12.dp))

            // 알림
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("알림 사용")
                Spacer(Modifier.width(8.dp))
                Switch(checked = ui.notifyEnabled, onCheckedChange = vm::toggleNotify)
            }
            if (ui.notifyEnabled) {
                Spacer(Modifier.height(8.dp))
                Text("알림 시간: ${ui.notifyTime ?: "--:--"}")
                Row {
                    Button(onClick = { vm.updateNotifyTime(LocalTime.of(9, 0)) }) { Text("오전 9시") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { vm.updateNotifyTime(LocalTime.now().withSecond(0).withNano(0)) }) { Text("지금 시각") }
                }
            }
            Spacer(Modifier.height(12.dp))

            // 태그 (프로젝트의 Chip/선택 UI로 대체)
            Text("태그: ${ui.tags.joinToString()}")
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
            ) { Text("추가") }

            Spacer(Modifier.height(20.dp))

            Button(
                enabled = !ui.loading,
                onClick = vm::submit,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (ui.loading) "저장 중..." else "저장") }

            ui.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }

}