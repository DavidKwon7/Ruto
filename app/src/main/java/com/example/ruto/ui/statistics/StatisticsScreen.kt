package com.example.ruto.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ruto.domain.routine.HeatmapDay
import io.ktor.websocket.Frame
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    navController: NavHostController,
    vm: StatisticsViewModel = hiltViewModel(),
    month: String = LocalDate.now().toString().substring(0,7)   // "YYYY-MM"
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(month) { vm.load(month = month) }

    Scaffold(
        topBar = { TopAppBar(title = { Frame.Text("월간 완료 현황 ($month)") }) }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                ui.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                ui.error != null -> Text("에러: ${ui.error}", modifier = Modifier.padding(24.dp))
                else -> HeatmapGrid(ui.heatmap)
            }
        }
    }
}

@Composable
private fun HeatmapGrid(heatmap: List<HeatmapDay>) {
    // 아주 단순한 7열 그리드 (요일 보정 X). 달력처럼 보정하려면 시작 요일 계산해서 앞에 빈칸 넣어주세요.
    val columns = 7
    val rows = (heatmap.size + columns - 1) / columns

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(heatmap.size) { idx ->
            val day = heatmap[idx]
            DayBox(day)
        }
        // 남는 칸 채우기(선택)
        val remain = rows * columns - heatmap.size
        if (remain > 0) {
            items(remain) {
                Box(Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun DayBox(day: HeatmapDay) {
    val fill = colorForPercent(day.percent)
    Column(
        modifier = Modifier
            .size(40.dp)
            .background(fill)
            .padding(2.dp)
    ) {
        Text(
            text = day.date.takeLast(2),  // 날짜 DD
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${day.percent}%",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun colorForPercent(p: Int): Color = when {
    p >= 80 -> Color(0xFF2ECC71) // 녹색
    p >= 50 -> Color(0xFF7DCEA0)
    p >= 20 -> Color(0xFFA9DFBF)
    p > 0   -> Color(0xFFD5F5E3)
    else    -> Color(0xFFEAECEE) // 회색
}