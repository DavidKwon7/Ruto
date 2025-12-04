package com.handylab.ruto.ui.state

import androidx.compose.runtime.Immutable

@Immutable
data class UiState(
    val loading: Boolean = false,
    val error: String? = null
)
