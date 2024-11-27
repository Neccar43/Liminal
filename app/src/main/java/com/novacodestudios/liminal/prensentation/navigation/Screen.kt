package com.novacodestudios.liminal.prensentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object Library : Screen()

    @Serializable
    data class Detail(val detailPageUrl: String, val typeString: String) : Screen()

    @Serializable
    data object NovelReading : Screen()

    /*@Serializable
    data class MangaReading(
        val chapters: List<UiChapter>,
        val currentChapter: UiChapter
    ) :
        Screen()*/

    @Serializable
    data object MangaReading:Screen()
}