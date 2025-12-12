package com.handylab.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.data.sync.CompleteQueue
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.usecase.ObserveRoutineListUseCase
import com.handylab.ruto.domain.routine.usecase.ObserveTodayCompletionIdsUseCase
import com.handylab.ruto.domain.routine.usecase.RefreshRoutinesUseCase
import com.handylab.ruto.domain.routine.usecase.SetRoutineCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@Immutable
data class RoutineListUiState(
    val loading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

@Immutable
data class Item(
    val routine: RoutineRead,
    val completedToday: Boolean
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RoutineListViewModel @Inject constructor(
    observeRoutineListUseCase: ObserveRoutineListUseCase,
    observeTodayCompletionIdsUseCase: ObserveTodayCompletionIdsUseCase,
    private val refreshRoutinesUseCase: RefreshRoutinesUseCase,
    private val setRoutineCompletionUseCase: SetRoutineCompletionUseCase,
    private val queue: CompleteQueue
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutineListUiState(loading = true))
    val uiState: StateFlow<RoutineListUiState> = _uiState.asStateFlow()

    private val doneToday = MutableStateFlow<Set<String>>(emptySet())

    init {
        // 루틴 목록 스트림
        observeRoutineListUseCase()
            .onEach { list ->
                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        items = list.map { r -> Item(r, completedToday = r.id in doneToday.value) }
                    )
                }
            }
            .launchIn(viewModelScope)

        // 오늘 완료 스트림
        observeTodayCompletionIdsUseCase()
            .onEach { set ->
                doneToday.value = set
                // 목록에 반영
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { it.copy(completedToday = it.routine.id in set) }
                    )
                }
            }
            .launchIn(viewModelScope)

        // 최초 서버 동기화 (화면 진입 시 1회)
        viewModelScope.launch {
            runCatching { refreshRoutinesUseCase() }
        }
    }

    // 수동 새로고침용
    fun refresh() {
        viewModelScope.launch {
            runCatching { refreshRoutinesUseCase() }
        }
    }

    // 완료 토글 (낙관적 반영 + 로컬 저장 + 완료면 큐 전송)
    fun toggleComplete(item: Item) {
        val r = item.routine
        val toCompleted = !item.completedToday

        _uiState.update { state ->
            state.copy(
                items = state.items.map { cur ->
                    if (cur.routine.id == r.id) cur.copy(completedToday = toCompleted) else cur
                }
            )
        }

        viewModelScope.launch {
            setRoutineCompletionUseCase(r.id, completed = toCompleted)
        }

        if (toCompleted) {
            viewModelScope.launch {
                queue.enqueue(routineId = r.id, completedAt = Instant.now())
            }
        }
    }
}