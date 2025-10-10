package com.example.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruto.data.routine.RoutineRepository
import com.example.ruto.domain.routine.RoutineRead
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineListUiState(
    val loading: Boolean = false,
    val items: List<RoutineRead> = emptyList(),
    val error: String? = null
)


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val repository: RoutineRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(RoutineListUiState(loading = true))
    val ui: StateFlow<RoutineListUiState> = _ui.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            repository.getRoutineList()
                .onSuccess { list -> _ui.update { it.copy(loading = false, items = list) } }
                .onFailure { e -> _ui.update { it.copy(loading = false, error = e.message) } }
        }
    }
}