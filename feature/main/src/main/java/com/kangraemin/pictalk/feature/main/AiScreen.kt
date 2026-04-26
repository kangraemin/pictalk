package com.kangraemin.pictalk.feature.main

import android.net.Uri
import android.os.Parcelable
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.model.GemmaSetupState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object AiScreen : Screen, Parcelable {

    data class State(
        val gemmaSetupState: GemmaSetupState = GemmaSetupState.Idle,
        val imageUri: Uri? = null,
        val labels: List<AacLabel> = emptyList(),
        val sentence: List<AacLabel> = emptyList(),
        val refinedSentence: String = "",
        val isRefining: Boolean = false,
        val isInferring: Boolean = false,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class OnImageSelected(val uri: Uri) : Event
        data class OnCardTapped(val label: AacLabel) : Event
        data object OnReadSentence : Event
        data object OnClearSentence : Event
        data object OnRetryGemmaSetup : Event
        data object OnBack : Event
    }
}
