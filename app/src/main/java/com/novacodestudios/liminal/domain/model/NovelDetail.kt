package com.novacodestudios.liminal.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelDetail(
    @SerialName("novel_name") override val name: String,
    @SerialName("novel_imageUrl") override val imageUrl: String,
    @SerialName("novel_author") override val author: String,
    val rate: String,
    @SerialName("novel_summary") override val summary: String,
    @SerialName("novel_chapters") override val chapters: List<Chapter>,
    @SerialName("novel_type") override val type: SeriesType = SeriesType.NOVEL
) : SeriesDetail(name, imageUrl, author, summary, chapters, type)