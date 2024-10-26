package com.novacodestudios.liminal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.component.BottomBar
import com.novacodestudios.liminal.prensentation.detail.DetailScreen
import com.novacodestudios.liminal.prensentation.mangaReading.MangaReadingScreen
import com.novacodestudios.liminal.prensentation.navigation.Graph
import com.novacodestudios.liminal.prensentation.navigation.Screen
import com.novacodestudios.liminal.prensentation.navigation.mainGraph
import com.novacodestudios.liminal.prensentation.novelReading.NovelReadingScreen
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import com.novacodestudios.liminal.util.encodeUrl
import com.novacodestudios.liminal.util.parcelableListType
import com.novacodestudios.liminal.util.parcelableType
import com.novacodestudios.liminal.util.withEncodedUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlin.reflect.typeOf

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiminalTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        bottomBar = {
                            /*val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination*/

                            BottomBar(navController = navController)
                        }
                    ) { paddingValues ->
                        NavHost(
                            modifier = Modifier.padding(paddingValues),
                            navController = navController,
                            startDestination = Graph.Main,
                            route = Graph.Root::class
                        ) {
                            mainGraph(navController)

                            composable<Screen.Detail>(
                                //typeMap = mapOf(typeOf<SeriesType>() to parcelableType<SeriesType>())
                            ) {
                                DetailScreen(
                                    onNavigateUp = { navController.navigateUp() },
                                    onNavigateMangaReadingScreen = { chapter, chapters ->
                                        Log.d(TAG, "onNavigateMangaReadingScreen: çalıştı $chapter")
                                        navController.navigate(
                                            Screen.MangaReading(
                                                currentChapter = chapter.withEncodedUrl(),
                                                chapters = chapters.map { it.withEncodedUrl() },
                                            )
                                        )
                                    },
                                    onNavigateNovelReadingScreen = { chapter, detailUrl ->
                                        navController.navigate(
                                            Screen.NovelReading(
                                                detailPageUrl = detailUrl.encodeUrl(),
                                                currentChapter = chapter.withEncodedUrl(),
                                            )
                                        )
                                    },
                                )
                            }

                            composable<Screen.MangaReading>(
                                typeMap = mapOf(
                                    typeOf<List<Chapter>>() to parcelableListType<Chapter>(),
                                    typeOf<Chapter>() to parcelableType<Chapter>()
                                )
                            ) {
                                MangaReadingScreen(onNavigateUp = {
                                    navController.navigateUp()
                                })
                            }
                            composable<Screen.NovelReading>(
                                typeMap = mapOf(
                                    typeOf<List<Chapter>>() to parcelableListType<Chapter>(),
                                    typeOf<Chapter>() to parcelableType<Chapter>()
                                )
                            ) {
                                NovelReadingScreen(onNavigateUp = {
                                    navController.navigateUp()
                                })
                            }
                        }
                    }


                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}