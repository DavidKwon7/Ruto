package com.handylab.ruto.ui.routine.edit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import com.handylab.ruto.domain.routine.usecase.DeleteRoutineUseCase
import com.handylab.ruto.domain.routine.usecase.FetchRoutineUseCase
import com.handylab.ruto.domain.routine.usecase.UpdateRoutineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class RoutineEditUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val routine: RoutineRead? = null,
    val name: String = "",
    val cadence: RoutineCadence = RoutineCadence.DAILY,
    val startDate: String = "",
    val endDate: String = "",
    val notifyEnabled: Boolean = false,
    val notifyTime: String? = null,
    val timezone: String = "",
    val tags: List<String> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class RoutineEditViewModel @Inject constructor(
    private val fetchRoutineUseCase: FetchRoutineUseCase,
    private val updateRoutineUseCase: UpdateRoutineUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase,
    savedState: SavedStateHandle
) : ViewModel() {
    private val routineId: String = checkNotNull(savedState["id"])

    private val _uiState = MutableStateFlow(RoutineEditUiState())
    val uiState: StateFlow<RoutineEditUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            fetchRoutineUseCase(routineId)
                .onSuccess { r ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            routine = r,
                            name = r.name,
                            cadence = r.cadence,
                            startDate = r.startDate,
                            endDate = r.endDate,
                            notifyEnabled = r.notifyEnabled,
                            notifyTime = r.notifyTime,
                            timezone = r.timezone,
                            tags = r.tags
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
        }
    }

    fun updateName(v: String) = _uiState.update { it.copy(name = v) }
    fun updateCadence(v: RoutineCadence) = _uiState.update { it.copy(cadence = v) }
    fun updateStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun updateEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun updateNotifyEnabled(v: Boolean) = _uiState.update { it.copy(notifyEnabled = v) }
    fun updateNotifyTime(v: String?) = _uiState.update { it.copy(notifyTime = v) }
    fun updateTags(v: List<String>) = _uiState.update { it.copy(tags = v.distinct()) }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null, saved = false) }

            val req = RoutineUpdateRequest(
                id = routineId,
                name = s.name,
                cadence = s.cadence,
                startDate = s.startDate,
                endDate = s.endDate,
                notifyEnabled = s.notifyEnabled,
                notifyTime = s.notifyTime,
                timezone = s.timezone,
                tags = s.tags
            )

            updateRoutineUseCase(req)
                .onSuccess { ok ->
                    _uiState.update { it.copy(saving = false, saved = ok) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(saving = false, error = e.message) }
                }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            deleteRoutineUseCase(routineId)
                .onSuccess { ok ->
                    _uiState.update { it.copy(saving = false) }
                    if (ok) onDone()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(saving = false, error = e.message) }
                }
        }
    }
}
