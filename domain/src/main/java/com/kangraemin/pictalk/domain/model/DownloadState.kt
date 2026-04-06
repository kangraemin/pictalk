package com.kangraemin.pictalk.domain.model

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progressPercent: Int) : DownloadState
    data object Complete : DownloadState
    data class Error(val message: String) : DownloadState
}
