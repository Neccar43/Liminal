package com.novacodestudios.liminal.prensentation.util

import android.util.Log
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.Source
import java.util.concurrent.TimeUnit

fun Source.getDisplayName(): String = this.name
    .replace('_', ' ')
    .lowercase()
    .split(' ')
    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

fun List<Chapter>.getNextChapter(currentChapter: Chapter): Chapter? {
    return try {
        val i = this.indexOf(currentChapter) + 1
        this[i]
    } catch (e: Exception) {
        Log.e(TAG, "getNextChapter: sıradaki bölüme geçerken hata aındı", e)
        null
    }
}

private const val TAG = "Extension"

fun List<Chapter>.getPreviousChapter(currentChapter: Chapter): Chapter? {
    return try {
        val i = this.indexOf(currentChapter) - 1
        this[i]
    } catch (e: Exception) {
        null
    }
}

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
