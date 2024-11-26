package com.novacodestudios.liminal.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Chapter(
    val title: String,
    val releaseDate: String,
    val url: String,
    val filePath:String?=null,// TODO: daha iyi isimlendir
) : Parcelable