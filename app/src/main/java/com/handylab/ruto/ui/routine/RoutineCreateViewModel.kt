package com.handylab.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.data.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineTag
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val _ui = MutableStateFlow(RoutineFormState())
    val ui: StateFlow<RoutineFormState> = _ui

    fun updateName(v: String) = _ui.update { it.copy(name = v) }
    fun updateCadence(v: RoutineCadence) = _ui.update { it.copy(cadence = v) }
    fun updateStartDate(v: LocalDate) = _ui.update { it.copy(startDate = v) }
    fun updateEndDate(v: LocalDate) = _ui.update { it.copy(endDate = v) }
    fun toggleNotify(on: Boolean) = _ui.update { it.copy(notifyEnabled = on, notifyTime = if (on) it.notifyTime else null) }
    fun updateNotifyTime(t: String) = _ui.update { it.copy(notifyTime = t) }
    fun updateTags(newTags: List<RoutineTag>) = _ui.update { it.copy(tags = newTags) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun submit() {
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, savedId = null) }
            repo.registerRoutine(
                name = s.name,
                cadence = s.cadence,
                startDate = s.startDate,
                endDate = s.endDate,
                notifyEnabled = s.notifyEnabled,
                notifyTime = s.notifyTime,
                tags = s.tags
            ).onSuccess { response ->
                _ui.update { it.copy(loading = false, savedId = response.id) }
            }.onFailure { exception ->
                _ui.update { it.copy(loading = false, error = exception.message) }
            }
        }
    }
}