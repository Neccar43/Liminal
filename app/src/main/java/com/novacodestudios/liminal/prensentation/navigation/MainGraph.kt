package com.novacodestudios.liminal.prensentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.home.HomeScreen
import com.novacodestudios.liminal.prensentation.library.LibraryScreen
import com.novacodestudios.liminal.util.encodeUrl
import com.novacodestudios.liminal.util.withEncodedUrl
import java.io.File

fun NavGraphBuilder.mainGraph(navController: NavController) {
    navigation<Graph.Main>(startDestination = Screen.Home) {
        composable<Screen.Home> {
            HomeScreen(onNavigateDetailScreen = { preview ->
                navController.navigate(
                    Screen.Detail(
                        preview.detailPageUrl,
                        preview.type.name
                    )
                )
            })
        }

        composable<Screen.Library> {
            LibraryScreen(
                onNavigateMangaReadingScreen = { chapter, chapters ->
                    NavArguments.currentChapter = chapter
                    NavArguments.chapterList = chapters

                    navController.navigate(
                        Screen.MangaReading
                    )
                },
                onNavigateNovelReadingScreen = { chapter, detailUrl ->
                    navController.navigate(
                        Screen.NovelReading(
                            detailPageUrl = detailUrl.encodeUrl(),
                            currentChapter = chapter.withEncodedUrl().toUiChapter(),
                        )
                    )
                }
            )
        }
    }
}

// TODO: Kaldır
object NavArguments {
    var currentChapter: Chapter? = null
    var chapterList: List<Chapter> = emptyList()
    var filesDir: File? = null
   // var seriesEntity: SeriesEntity? = null
}



