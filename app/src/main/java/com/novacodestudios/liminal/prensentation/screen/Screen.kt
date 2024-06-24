package com.novacodestudios.liminal.prensentation.screen

import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.SeriesType
import kotlinx.serialization.Serializable

/*@Serializable
object HomeScreen

@Serializable
data class DetailScreen(val detailPageUrl: String, val type: SeriesType)

// TODO: Burada sadece chapter nesenesi al
@Serializable
data class NovelReadingScreen(val currentChapter: Chapter, val detailPageUrl: String)

@Serializable
data class MangaReadingScreen(val chapters: List<Chapter>, val currentChapter: Chapter)

@Serializable
object LibraryScreen*/

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object Library : Screen()

    @Serializable
    data class Detail(val detailPageUrl: String, val type: SeriesType) : Screen()

    @Serializable
    data class NovelReading(val currentChapter: Chapter, val detailPageUrl: String) : Screen()

    @Serializable
    data class MangaReading(val chapters: List<Chapter>, val currentChapter: Chapter) :
        Screen()
}