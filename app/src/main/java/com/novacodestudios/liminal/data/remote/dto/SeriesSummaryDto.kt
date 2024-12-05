package com.novacodestudios.liminal.data.remote.dto

import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.Source

data class SeriesSummaryDto(
    val name: String,
    val imageUrl: String,
    val detailPageUrl: String,
    val source: Source,
    val seriesType: SeriesType
)
