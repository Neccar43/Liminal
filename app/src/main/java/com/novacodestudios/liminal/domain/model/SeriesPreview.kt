package com.novacodestudios.liminal.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class SeriesPreview (
    @SerialName("name") open val name: String,
    @SerialName("imageUrl") open val imageUrl: String,
    @SerialName("detailPageUrl") open val detailPageUrl: String,
    @SerialName("source") open val source: String,
    @SerialName("type") open val type: SeriesType
)

