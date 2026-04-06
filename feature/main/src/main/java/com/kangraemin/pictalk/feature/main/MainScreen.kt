package com.kangraemin.pictalk.feature.main

import android.net.Uri
import android.os.Parcelable
import com.kangraemin.pictalk.domain.model.AacLabel
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object MainScreen : Screen, Parcelable {

    sealed interface Status {
        data object CheckingModel : Status
        data class Downloading(val progressPercent: Int) : Status
        data object InitializingModel : Status
        data class Ready(val message: String) : Status
        data class Error(val message: String) : Status
    }

    data class State(
        val status: Status = Status.CheckingModel,
        val imageUri: Uri? = null,
        val labels: List<AacLabel> = emptyList(),
        val isInferring: Boolean = false,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object OnRetryDownload : Event
        data class OnImageSelected(val uri: Uri) : Event
        data class OnCardClicked(val label: AacLabel) : Event
    }
}
