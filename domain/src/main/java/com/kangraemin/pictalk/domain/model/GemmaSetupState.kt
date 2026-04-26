package com.kangraemin.pictalk.domain.model

sealed interface GemmaSetupState {
    data object Idle : GemmaSetupState
    data class Downloading(val progressPercent: Int) : GemmaSetupState
    data object Initializing : GemmaSetupState
    data object Ready : GemmaSetupState
    data class Error(val message: String) : GemmaSetupState
}
