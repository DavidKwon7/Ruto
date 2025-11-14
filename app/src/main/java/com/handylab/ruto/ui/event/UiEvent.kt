package com.handylab.ruto.ui.event

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}