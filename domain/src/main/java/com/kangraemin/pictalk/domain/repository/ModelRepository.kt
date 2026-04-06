package com.kangraemin.pictalk.domain.repository

import com.kangraemin.pictalk.domain.model.DownloadState
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun isModelReady(): Boolean
    fun modelPath(): String
    fun downloadModel(): Flow<DownloadState>
}
