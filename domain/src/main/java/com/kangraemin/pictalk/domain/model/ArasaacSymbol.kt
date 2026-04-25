package com.kangraemin.pictalk.domain.model

data class ArasaacSymbol(
    val id: Int,
    val keyword: String,
    val tags: List<String>,
    val categories: List<String>,
    val localImagePath: String,
)
