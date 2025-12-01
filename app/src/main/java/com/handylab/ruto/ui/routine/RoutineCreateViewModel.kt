package com.handylab.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.data.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineTag
import com.handylab.ruto.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
data class RoutineFormState(
    val name: String = "",
    val cadence: RoutineCadence = RoutineCadence.DAILY,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusMonths(1),
    val notifyEnabled: Boolean = false,
    val notifyTime: String? = null,
    val tags: List<RoutineTag> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val savedId: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RoutineCreateViewModel @Inject constructor(
    private val repo: RoutineRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutineFormState())
    val uiState: StateFlow<RoutineFormState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: MutableSharedFlow<UiEvent> = _uiEvent

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateCadence(value: RoutineCadence) = _uiState.update { it.copy(cadence = value) }
    fun updateStartDate(startDate: LocalDate) = _uiState.update { it.copy(startDate = startDate) }
    fun updateEndDate(endDate: LocalDate) = _uiState.update { it.copy(endDate = endDate) }
    fun toggleNotify(on: Boolean) = _uiState.update { it.copy(notifyEnabled = on, notifyTime = if (on) it.notifyTime else null) }
    fun updateNotifyTime(time: String) = _uiState.update { it.copy(notifyTime = time) }
    fun updateTags(newTags: List<RoutineTag>) = _uiState.update { it.copy(tags = newTags) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun submit() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, savedId = null) }
            repo.registerRoutine(
                name = s.name,
                cadence = s.cadence,
                startDate = s.startDate,
                endDate = s.endDate,
                notifyEnabled = s.notifyEnabled,
                notifyTime = s.notifyTime,
                tags = s.tags
            ).onSuccess { response ->
                _uiState.update { it.copy(loading = false, savedId = response.id) }
            }.onFailure { exception ->
                // _uiState.update { it.copy(loading = false, error = exception.message) }
                showMessage(exception.message ?: "루틴 등록 실패")
            }
        }
    }

    private suspend fun showMessage(msg: String) {
        _uiState.update { it.copy(error = msg, loading = false) }
        _uiEvent.emit(UiEvent.ShowToastMsg(msg))
    }
}