package com.kangraemin.pictalk.feature.main

import android.os.Parcelable
import com.kangraemin.pictalk.domain.model.AacCategory
import com.kangraemin.pictalk.domain.model.AacLabel
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryScreen(val categoryId: String) : Screen, Parcelable {

    data class State(
        val category: AacCategory? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class OnCardTapped(val label: AacLabel) : Event
        data object OnBack : Event
    }
}
