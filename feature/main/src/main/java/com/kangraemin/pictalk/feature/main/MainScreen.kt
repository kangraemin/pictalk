package com.kangraemin.pictalk.feature.main

import android.os.Parcelable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object MainScreen : Screen, Parcelable {

    sealed interface Status {
        data object CheckingSymbols : Status
        data class DownloadingSymbols(
            val progressPercent: Int,
            val downloaded: Int,
            val total: Int,
        ) : Status
        data class Error(val message: String) : Status
    }

    data class State(
        val status: Status = Status.CheckingSymbols,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data object OnRetryDownload : Event
    }
}
