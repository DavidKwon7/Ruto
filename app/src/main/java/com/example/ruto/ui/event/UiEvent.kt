package com.example.ruto.ui.event

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}