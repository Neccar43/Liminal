package com.novacodestudios.liminal.domain.model

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Parcelize
data class Chapter(
    val title:String,
    val releaseDate:String,
    val url:String,
) : Parcelable