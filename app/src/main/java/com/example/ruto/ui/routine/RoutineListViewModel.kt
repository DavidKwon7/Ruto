package com.example.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruto.data.routine.RoutineRepository
import com.example.ruto.data.sync.CompleteQueue
import com.example.ruto.domain.routine.RoutineRead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class RoutineListUiState(
    val loading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

data class Item(
    val routine: RoutineRead,
    val completedToday: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val queue: CompleteQueue
) : ViewModel() {
    private val _ui = MutableStateFlow(RoutineListUiState(loading = true))
    val ui: StateFlow<RoutineListUiState> = _ui.asStateFlow()

    private var listCache: List<RoutineRead> = emptyList()
    private var completesJob: Job? = null

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            repository.getRoutineList()
                .onSuccess { list ->
                    listCache = list
                    // 완료 캐시와 결합 스트림 구독
                    completesJob?.cancel()
                    completesJob = repository.observeTodayCompletions()
                        .onEach { completes ->
                            val doneSet = completes.filter { it.completed }.map { it.routineId }.toSet()
                            _ui.update {
                                it.copy(
                                    loading = false,
                                    items = listCache.map { r -> Item(routine = r, completedToday = r.id in doneSet) }
                                )
                            }
                        }
                        .launchIn(viewModelScope)
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message) }
                }
        }
    }

    fun toggleComplete(item: Item) {
        val r = item.routine
        val toCompleted = !item.completedToday

        // 1) 낙관적 UI 업데이트 (즉시 색상 변경)
        _ui.update { state ->
            state.copy(
                items = state.items.map { it ->
                    if (it.routine.id == r.id) it.copy(completedToday = true) else it
                }
            )
        }

        // 2) 로컬 DB에 영속 반영 → 화면 재입장/프로세스 재시작에도 유지
        viewModelScope.launch {
            repository.setCompletionLocal(r.id, completed = toCompleted)
        }

        // 3) 완료(true)일 때만 서버 전송 큐에 추가 (해제는 로컬만)
        if (toCompleted) {
            viewModelScope.launch {
                queue.enqueue(routineId = r.id, completedAt = Instant.now())
            }
        }
    }
}