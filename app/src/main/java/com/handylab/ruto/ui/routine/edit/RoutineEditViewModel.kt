package com.handylab.ruto.ui.routine.edit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.data.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


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
    private val repository: RoutineRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    private val routineId: String = checkNotNull(savedState["id"])

    private val _ui = MutableStateFlow(RoutineEditUiState())
    val ui: StateFlow<RoutineEditUiState> = _ui.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            repository.getRoutine(routineId)
                .onSuccess { r ->
                    _ui.update {
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
                    _ui.update { it.copy(loading = false, error = e.message) }
                }
        }
    }

    fun updateName(v: String) = _ui.update { it.copy(name = v) }
    fun updateCadence(v: RoutineCadence) = _ui.update { it.copy(cadence = v) }
    fun updateStartDate(v: String) = _ui.update { it.copy(startDate = v) }
    fun updateEndDate(v: String) = _ui.update { it.copy(endDate = v) }
    fun updateNotifyEnabled(v: Boolean) = _ui.update { it.copy(notifyEnabled = v) }
    fun updateNotifyTime(v: String?) = _ui.update { it.copy(notifyTime = v) }
    fun updateTags(v: List<String>) = _ui.update { it.copy(tags = v.distinct()) }

    fun save() {
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(saving = true, error = null, saved = false) }

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

            repository.updateRoutine(req)
                .onSuccess { ok ->
                    _ui.update { it.copy(saving = false, saved = ok) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(saving = false, error = e.message) }
                }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(saving = true, error = null) }
            repository.deleteRoutine(routineId)
                .onSuccess { ok ->
                    _ui.update { it.copy(saving = false) }
                    if (ok) onDone()
                }
                .onFailure { e ->
                    _ui.update { it.copy(saving = false, error = e.message) }
                }
        }
    }
}
