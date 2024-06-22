package com.novacodestudios.liminal.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MangaPreview(
    @SerialName("manga_name") override val name: String,
    @SerialName("manga_imageUrl") override val imageUrl: String,
    @SerialName("manga_detailPageUrl") override val detailPageUrl: String,
    @SerialName("manga_source") override val source: String,
    @SerialName("manga_type") override val type: SeriesType = SeriesType.MANGA
) : SeriesPreview(name, imageUrl, detailPageUrl, source, type)