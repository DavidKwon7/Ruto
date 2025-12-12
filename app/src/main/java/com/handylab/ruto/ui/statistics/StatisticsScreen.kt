package com.handylab.ruto.ui.statistics

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
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.handylab.ruto.domain.routine.HeatmapDay
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    navController: NavHostController,
    vm: StatisticsViewModel = hiltViewModel(),
    month: String = LocalDate.now().toString().substring(0, 7) // "YYYY-MM"
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val tz = remember { ZoneId.systemDefault().id }

    LaunchedEffect(month, tz) {
        vm.startObserving(month, tz)
    }

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
                    item(key = "heatmap") {
                        HeatmapGrid(
                            heatmap = ui.heatmap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 240.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            color = colorScheme.outlineVariant
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    items(
                        items = ui.routineDays,
                        key = { it.routineId }
                    ) { rd ->
                        RoutineRowItem(
                            title = rd.name.ifBlank { "제목 없음" },
                            monthDays = ui.heatmap.size,
                            days = rd.days
                        )
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HeatmapGrid(
    heatmap: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    val columns = 7
    val rows = if (heatmap.isEmpty()) 0 else (heatmap.size + columns - 1) / columns
    val cell = 40.dp
    val gap = 8.dp
    val gridHeight = if (rows <= 0) 140.dp else rows * cell + (maxOf(rows - 1, 0)) * gap

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(gridHeight),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalArrangement = Arrangement.spacedBy(gap),
        userScrollEnabled = false
    ) {
        items(heatmap.size) { idx ->
            DayBox(heatmap[idx])
        }
        val remain = maxOf(rows * columns - heatmap.size, 0)
        if (remain > 0) {
            items(remain) { Box(Modifier.size(40.dp)) }
        }
    }
}

@Composable
private fun DayBox(day: HeatmapDay) {
    val colorScheme = colorScheme
    val fill = colorForPercent(day.safePercent, colorScheme)
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
            color = colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${day.safePercent}%",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RoutineRowItem(
    title: String,
    monthDays: Int,
    days: List<Int>
) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))

        // 일자 칸이 화면보다 길 수 있으니 가로 스크롤
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
                            if (v == 1) colorScheme.primaryContainer else colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            color = colorScheme.outlineVariant
        )
        Spacer(Modifier.height(8.dp))
    }
}

private fun colorForPercent(p: Int, colorScheme: ColorScheme): Color {
    val primaryContainer = colorScheme.primaryContainer
    val variants = listOf(
        primaryContainer.copy(alpha = 0.35f),
        primaryContainer.copy(alpha = 0.55f),
        primaryContainer.copy(alpha = 0.75f),
        primaryContainer
    )

    return when {
        p >= 80 -> variants[3]
        p >= 50 -> variants[2]
        p >= 20 -> variants[1]
        p > 0 -> variants[0]
        else -> colorScheme.surfaceVariant
    }
}