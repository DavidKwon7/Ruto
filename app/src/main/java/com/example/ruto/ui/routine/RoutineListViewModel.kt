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

    private val doneToday = MutableStateFlow<Set<String>>(emptySet())


    init {
        // 루틴 목록 스트림
        repository.observeRoutineList()
            .onEach { list ->
                _ui.update { state ->
                    state.copy(
                        loading = false,
                        items = list.map { r -> Item(r, completedToday = r.id in doneToday.value) }
                    )
                }
            }
            .launchIn(viewModelScope)

        // 오늘 완료 스트림
        repository.observeTodayCompletions()
            .onEach { rows ->
                val set = rows.filter { it.completed }.map { it.routineId }.toSet()
                doneToday.value = set
                // 목록에 반영
                _ui.update { state ->
                    state.copy(
                        items = state.items.map { it.copy(completedToday = it.routine.id in set) }
                    )
                }
            }
            .launchIn(viewModelScope)

        // 최초 서버 동기화 (화면 진입 시 1회)
        viewModelScope.launch {
            runCatching { repository.refreshFromServer() }
        }
    }

    /** 수동 새로고침용 */
    fun refresh() {
        viewModelScope.launch {
            runCatching { repository.refreshFromServer() }
        }
    }

    /** 완료 토글 (낙관적 반영 + 로컬 저장 + 완료면 큐 전송) */
    fun toggleComplete(item: Item) {
        val r = item.routine
        val toCompleted = !item.completedToday

        _ui.update { state ->
            state.copy(
                items = state.items.map { cur ->
                    if (cur.routine.id == r.id) cur.copy(completedToday = toCompleted) else cur
                }
            )
        }

        viewModelScope.launch {
            repository.setCompletionLocal(r.id, completed = toCompleted)
        }

        if (toCompleted) {
            viewModelScope.launch {
                queue.enqueue(routineId = r.id, completedAt = Instant.now())
            }
        }
    }
}