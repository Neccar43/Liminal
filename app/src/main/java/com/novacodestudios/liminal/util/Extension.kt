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
import java.security.MessageDigest
import java.util.Locale
import android.text.format.DateUtils
import java.util.concurrent.TimeUnit

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

fun String.hashToMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(this.toByteArray())
    return digested.joinToString("") { String.format("%02x", it) }
}

fun Chapter.withEncodedUrl():Chapter= this.copy(url = this.url.encodeUrl())


fun formatTimeAgo(timeInMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeInMillis

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "az önce"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "$minutes dakika önce"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "$hours saat önce"
        }
        diff < TimeUnit.DAYS.toMillis(30) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "$days gün önce"
        }
        diff < TimeUnit.DAYS.toMillis(365) -> {
            val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
            "$months ay önce"
        }
        else -> {
            val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
            "$years yıl önce"
        }
    }
}

