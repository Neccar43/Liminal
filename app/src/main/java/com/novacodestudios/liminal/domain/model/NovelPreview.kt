package com.novacodestudios.liminal.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelPreview(
    @SerialName("novel_name") override val name: String,
    @SerialName("novel_imageUrl") override val imageUrl: String,
    @SerialName("novel_detailPageUrl") override val detailPageUrl: String,
    @SerialName("novel_source") override val source: String,
    @SerialName("novel_type") override val type: SeriesType = SeriesType.NOVEL
) : SeriesPreview(name, imageUrl, detailPageUrl, source, type)

