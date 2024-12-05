package com.novacodestudios.liminal.domain.model

data class SeriesSummary(
    val name: String,
    val imageUrl: String,
    val detailPageUrl: String,
    val source: Source,
    val type: SeriesType,
)
