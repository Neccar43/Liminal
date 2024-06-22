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