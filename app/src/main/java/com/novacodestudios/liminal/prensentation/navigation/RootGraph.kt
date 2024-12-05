package com.novacodestudios.liminal.prensentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.novacodestudios.liminal.prensentation.detail.DetailScreen
import com.novacodestudios.liminal.prensentation.mangaReading.MangaReadingScreen
import com.novacodestudios.liminal.prensentation.novelReading.NovelReadingScreen

@Composable
fun LiminalNavHost(modifier: Modifier, navController: NavHostController) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Graph.Main,
        route = Graph.Root::class
    ) {
        mainGraph(navController)

        composable<Screen.Detail> {
            DetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateMangaReadingScreen = { chapterId ->
                    Log.d(
                        TAG,
                        "onNavigateMangaReadingScreen: çalıştı $chapterId"
                    )
                    navController.navigate(Screen.MangaReading(chapterId))
                },
                onNavigateNovelReadingScreen = { chapterId ->
                    navController.navigate(Screen.NovelReading(chapterId))
                },
            )
        }

        composable<Screen.MangaReading> {
            MangaReadingScreen(onNavigateUp = {
                navController.navigateUp()
            })
        }
        composable<Screen.NovelReading> {
            NovelReadingScreen(onNavigateUp = {
                navController.navigateUp()
            })
        }
    }
}

private const val TAG = "RootGraph"