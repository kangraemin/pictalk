package com.kangraemin.pictalk.domain.repository

import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.model.GemmaSetupState
import kotlinx.coroutines.flow.StateFlow

interface GemmaRepository {
    val setupState: StateFlow<GemmaSetupState>
    suspend fun setup()
    suspend fun suggestLabels(imageDescription: String): List<AacLabel>
    fun isReady(): Boolean
}
