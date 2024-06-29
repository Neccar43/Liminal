package com.novacodestudios.liminal.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
enum class SeriesType : Parcelable {
    MANGA,
    NOVEL
}

fun String.toType(): SeriesType =
    SeriesType.entries.find { it.name == this } ?: throw IllegalArgumentException()