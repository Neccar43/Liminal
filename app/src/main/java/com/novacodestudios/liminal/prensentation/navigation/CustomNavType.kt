@file:Suppress("DEPRECATION")

package com.novacodestudios.liminal.prensentation.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.novacodestudios.liminal.domain.model.Chapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object CustomNavType {

/*    val ChapterType= object : NavType<Chapter> (
        isNullableAllowed = false
    ){
        override fun get(bundle: Bundle, key: String): Chapter? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun serializeAsValue(value: Chapter): String {
            return Uri.encode(Json.encodeToString(value))
        }

        override fun parseValue(value: String): Chapter {
            return Json.decodeFromString<Chapter>(value)
        }

        override fun put(bundle: Bundle, key: String, value: Chapter) {
            bundle.putString(key, Json.encodeToString(value))
        }

    }*/



    /*val ChapterType = object : NavType<UiChapter>(
        isNullableAllowed = false
    ) {
        override fun put(bundle: Bundle, key: String, value: UiChapter) {
            bundle.putParcelable(key, value)
        }
        override fun get(bundle: Bundle, key: String): UiChapter {
            return bundle.getParcelable(key) ?: throw IllegalArgumentException("Bundle does not contain a Chapter with key $key")
        }

        override fun serializeAsValue(value: UiChapter): String {
            // Serialized values must always be Uri encoded
            return Uri.encode(Json.encodeToString(value))
        }

        override fun parseValue(value: String): UiChapter {
            return Json.decodeFromString<UiChapter>(value)
        }
    }*/

    val UiChapterType= object : NavType<UiChapter> (
        isNullableAllowed = false
    ){
        override fun get(bundle: Bundle, key: String): UiChapter? {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun serializeAsValue(value: UiChapter): String {
            return Uri.encode(Json.encodeToString(value))
        }

        override fun parseValue(value: String): UiChapter {
            return Json.decodeFromString<UiChapter>(value)
        }

        override fun put(bundle: Bundle, key: String, value: UiChapter) {
            bundle.putString(key, Json.encodeToString(value))
        }

    }

    val UiChapterListType = object : NavType<ArrayList<UiChapter>>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): ArrayList<UiChapter>? {
            val jsonString = bundle.getString(key) ?: return null
            return Json.decodeFromString<List<UiChapter>>(jsonString).let { ArrayList(it) }
        }

        override fun serializeAsValue(value: ArrayList<UiChapter>): String {
            return Uri.encode(Json.encodeToString(value))
        }

        override fun parseValue(value: String): ArrayList<UiChapter> {
            return Json.decodeFromString<List<UiChapter>>(value).let { ArrayList(it) }
        }

        override fun put(bundle: Bundle, key: String, value: ArrayList<UiChapter>) {
            bundle.putString(key, Json.encodeToString(value))
        }
    }

}

@Serializable
data class UiChapter(
    val title: String,
    val releaseDate: String,
    val url: String,
    val filepath:String?
)

fun UiChapter.toChapter()=
    Chapter(title,releaseDate,url,filepath)

fun Chapter.toUiChapter()=
    UiChapter(title,releaseDate,url,filePath)

