package com.example.ruto.ui.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruto.data.routine.RoutineMonthlyRepository
import com.example.ruto.data.statistics.model.HeatmapDay
import com.example.ruto.data.statistics.model.RoutineDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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

    private var job: Job? = null

    fun startObserving(month: String, tz: String = ZoneId.systemDefault().id) {
        job?.cancel()
        _ui.update { it.copy(loading = true, month = month, tz = tz, error = null) }
        job = repo.observeMonthly(tz, month)
            .onEach { res ->
                _ui.update { it.copy(
                    loading = false,
                    heatmap = res.heatmap,
                    routineDays = res.routines,
                    error = null
                ) }
            }
            .catch { e -> _ui.update { it.copy(loading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }
}