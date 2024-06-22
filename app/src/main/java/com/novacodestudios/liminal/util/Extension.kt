package com.novacodestudios.liminal.util

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.novacodestudios.liminal.domain.model.Chapter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}

inline fun <reified T : Parcelable> parcelableType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }

    override fun parseValue(value: String): T = json.decodeFromString(value)

    override fun serializeAsValue(value: T): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: T) = bundle.putParcelable(key, value)
}

inline fun <reified T : Parcelable> parcelableListType(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<List<T>>(isNullableAllowed = isNullableAllowed) {
    override fun get(bundle: Bundle, key: String): List<T>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelableArrayList(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelableArrayList(key)
        }

    override fun parseValue(value: String): List<T> = json.decodeFromString(value)

    override fun serializeAsValue(value: List<T>): String = json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: List<T>) = bundle.putParcelableArrayList(key, ArrayList(value))
}



fun String.encodeUrl():String= URLEncoder.encode(this, StandardCharsets.UTF_8.toString())

fun List<Chapter>.getNextChapter(currentChapter: Chapter): Chapter? {
    return try {
        val i = this.indexOf(currentChapter) + 1
        this[i]
    } catch (e: Exception) {
        null
    }


}

fun List<Chapter>.getPreviousChapter(currentChapter: Chapter): Chapter? {
    return try {
        val i = this.indexOf(currentChapter) - 1
        this[i]
    } catch (e: Exception) {
        null
    }
}