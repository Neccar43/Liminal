package com.novacodestudios.liminal.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaDetail(
    @SerialName("manga_name") override val name: String,
    @SerialName("manga_imageUrl") override val imageUrl: String,
    @SerialName("manga_summary") override val summary: String,
    @SerialName("manga_author") override val author: String,
    @SerialName("manga_chapters") override val chapters: List<Chapter>,
    @SerialName("manga_type") override val type: SeriesType = SeriesType.MANGA,
) : SeriesDetail(name, imageUrl, author, summary, chapters, type)