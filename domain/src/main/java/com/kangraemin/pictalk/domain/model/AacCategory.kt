package com.kangraemin.pictalk.domain.model

data class AacCategory(
    val id: String,
    val name: String,
    val iconSymbolId: Int,
    val labels: List<AacLabel>,
)
