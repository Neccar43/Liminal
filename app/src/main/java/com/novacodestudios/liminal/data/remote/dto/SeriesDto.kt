package com.novacodestudios.liminal.data.remote.dto

import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.model.Tag

data class SeriesDto(
    val name: String,
    val imageUrl: String,
    val summary: String,
    val author: String,
    val chapters: List<ChapterDto>,
    val status: String,
    val tags: List<Tag>,
    val source: Source,
    val seriesType: SeriesType,
    val detailPageUrl: String,
)
