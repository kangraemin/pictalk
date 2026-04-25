package com.kangraemin.pictalk.domain.repository

import com.kangraemin.pictalk.domain.model.AacCategory
import com.kangraemin.pictalk.domain.model.ArasaacDownloadState
import com.kangraemin.pictalk.domain.model.ArasaacSymbol
import kotlinx.coroutines.flow.Flow

interface ArasaacRepository {
    fun isReady(): Boolean
    fun downloadAll(): Flow<ArasaacDownloadState>
    fun getCategories(): List<AacCategory>
    fun getCategory(id: String): AacCategory?
    fun search(query: String): List<ArasaacSymbol>
    fun findByKeyword(keyword: String): ArasaacSymbol?
}
