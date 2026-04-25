package com.kangraemin.pictalk.feature.main

import android.os.Parcelable
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
        data object CheckingSymbols : Status
        data class DownloadingSymbols(
            val progressPercent: Int,
            val downloaded: Int,
            val total: Int,
        ) : Status
        data class Ready(val message: String) : Status
        data class Error(val message: String) : Status
    }

    data class State(
        val status: Status = Status.CheckingModel,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object OnRetryDownload : Event
    }
}
