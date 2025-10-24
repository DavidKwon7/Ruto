package com.example.ruto.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruto.data.routine.RoutineMonthlyRepository
import com.example.ruto.domain.routine.HeatmapDay
import com.example.ruto.domain.routine.MonthlyCompletionsResponse
import com.example.ruto.domain.routine.RoutineDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
data class MonthlyUiState(
    val loading: Boolean = false,
    val month: String = "",               // "YYYY-MM"
    val tz: String = ZoneId.systemDefault().id,
    val heatmap: List<HeatmapDay> = emptyList(),
    val routineDays: List<RoutineDays> = emptyList(),
    val error: String? = null
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class StatisticsViewModel @Inject constructor(
    private val repo: RoutineMonthlyRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(MonthlyUiState())
    val ui: StateFlow<MonthlyUiState> = _ui.asStateFlow()

    fun load(month: String = LocalDate.now().toString().substring(0,7), tz: String = ZoneId.systemDefault().id) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, month = month, tz = tz) }
            runCatching {
                repo.getMonthly(tz = tz, month = month)
            }.onSuccess { res: MonthlyCompletionsResponse ->
                _ui.update { it.copy(loading = false, heatmap = res.heatmap, routineDays = res.routines) }
            }.onFailure { e ->
                _ui.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

}