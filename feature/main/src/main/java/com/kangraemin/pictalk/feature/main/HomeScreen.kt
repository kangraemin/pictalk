package com.kangraemin.pictalk.feature.main

import android.os.Parcelable
import com.kangraemin.pictalk.domain.model.AacCategory
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object HomeScreen : Screen, Parcelable {

    data class State(
        val categories: List<AacCategory> = emptyList(),
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class OnCategorySelected(val categoryId: String) : Event
        data object OnAiSelected : Event
    }
}
