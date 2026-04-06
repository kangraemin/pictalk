package com.kangraemin.pictalk.domain.repository

import com.kangraemin.pictalk.domain.model.AacLabel

interface GemmaRepository {
    suspend fun initialize(modelPath: String)
    suspend fun suggestLabels(imageDescription: String): List<AacLabel>
    fun isReady(): Boolean
}
