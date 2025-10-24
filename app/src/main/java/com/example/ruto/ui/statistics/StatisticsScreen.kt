package com.example.ruto.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    navController: NavHostController,
    vm: StatisticsViewModel = hiltViewModel(),
    month: String = LocalDate.now().toString().substring(0, 7) // "YYYY-MM"
) {
    val ui by vm.ui.collectAsState()
    LaunchedEffect(month) { vm.load(month = month) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("월간 완료 현황 ($month)") }) }
    ) { pad ->
        when {
            ui.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
            ui.error != null -> Text("에러: ${ui.error}", modifier = Modifier.padding(24.dp))
            else -> {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pad),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // 1) Heatmap를 리스트 헤더로
                    item(key = "heatmap") {
                        HeatmapGrid(
                            heatmap = ui.heatmap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 240.dp) // 중요: 전체 높이 점유 금지
                        )
                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))
                    }

                    // 2) 루틴 행들
                    items(
                        items = ui.routineDays,
                        key = { it.routineId } // 성능/상태 보존
                    ) { rd ->
                        RoutineRowItem(
                            title = rd.name.ifBlank { "제목 없음" },
                            monthDays = ui.heatmap.size, // 월 일수(heatmap 크기와 동일 가정)
                            days = rd.days
                        )
                    }

                    item { Spacer(Modifier.height(24.dp)) } // 바닥 여백
                }
            }
        }
    }
}

/** Heatmap: LazyVerticalGrid에서 fillMaxSize() 제거 & 높이 한정 */
@Composable
private fun HeatmapGrid(
    heatmap: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    val columns = 7
    val rows = (heatmap.size + columns - 1) / columns

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier, // <-- fillMaxSize() 없앰
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = true
    ) {
        items(heatmap.size) { idx ->
            DayBox(heatmap[idx])
        }
        val remain = rows * columns - heatmap.size
        if (remain > 0) {
            items(remain) { Box(Modifier.size(40.dp)) }
        }
    }
}

@Composable
private fun DayBox(day: HeatmapDay) {
    val fill = colorForPercent(day.percent)
    Column(
        modifier = Modifier
            .size(40.dp)
            .background(fill, shape = RoundedCornerShape(6.dp))
            .padding(2.dp)
    ) {
        Text(
            text = day.date.takeLast(2),
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

/** 각 루틴 한 줄 */
@Composable
private fun RoutineRowItem(
    title: String,
    monthDays: Int,
    days: List<Int>
) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))

        // 일자 칸이 화면보다 길 수 있으니 가로 스크롤 허용(옵션)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.take(monthDays).forEach { v ->
                Box(
                    Modifier
                        .size(12.dp)
                        .background(
                            if (v == 1) Color(0xFF2ECC71) else Color(0xFFEAECEE),
                            shape = CircleShape
                        )
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
    }
}

private fun colorForPercent(p: Int): Color = when {
    p >= 80 -> Color(0xFF2ECC71)
    p >= 50 -> Color(0xFF7DCEA0)
    p >= 20 -> Color(0xFFA9DFBF)
    p > 0   -> Color(0xFFD5F5E3)
    else    -> Color(0xFFEAECEE)
}
