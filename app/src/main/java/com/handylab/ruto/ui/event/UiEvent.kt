package com.handylab.ruto.ui.event

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowToastMsg(val message: String) : UiEvent
}