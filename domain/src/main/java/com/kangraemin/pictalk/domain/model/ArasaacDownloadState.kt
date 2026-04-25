package com.kangraemin.pictalk.domain.model

sealed interface ArasaacDownloadState {
    data object Idle : ArasaacDownloadState
    data class DownloadingMetadata(val progressPercent: Int) : ArasaacDownloadState
    data class DownloadingImages(
        val progressPercent: Int,
        val downloaded: Int,
        val total: Int,
    ) : ArasaacDownloadState
    data object Complete : ArasaacDownloadState
    data class Error(val message: String) : ArasaacDownloadState
}
