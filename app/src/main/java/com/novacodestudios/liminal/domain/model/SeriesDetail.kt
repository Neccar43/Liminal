package com.novacodestudios.liminal.domain.model

import kotlinx.serialization.Serializable

@Serializable
open class SeriesDetail (
    open val name: String,
    open val imageUrl: String,
    open val author: String,
    open val summary: String,
    open val chapters: List<Chapter>,
    open val type:SeriesType,
)