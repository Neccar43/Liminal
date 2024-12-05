package com.novacodestudios.liminal.prensentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.novacodestudios.liminal.prensentation.home.HomeScreen
import com.novacodestudios.liminal.prensentation.library.LibraryScreen

fun NavGraphBuilder.mainGraph(navController: NavController) {
    navigation<Graph.Main>(startDestination = Screen.Home) {
        composable<Screen.Home> {
            HomeScreen(onNavigateDetailScreen = { preview ->
                navController.navigate(
                    Screen.Detail(
                        preview.detailPageUrl,
                        preview.type.name,
                    )
                )
            })
        }

        composable<Screen.Library> {
            LibraryScreen(
                onNavigateMangaReadingScreen = { chapterId ->
                    navController.navigate(
                        Screen.MangaReading(chapterId)
                    )
                },
                onNavigateNovelReadingScreen = { chapterId ->
                    navController.navigate(
                        Screen.NovelReading(chapterId)
                    )
                }
            )
        }
    }
}



