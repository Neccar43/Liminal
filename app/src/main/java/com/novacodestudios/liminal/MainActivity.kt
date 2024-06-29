package com.novacodestudios.liminal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.detail.DetailScreen
import com.novacodestudios.liminal.prensentation.home.HomeScreen
import com.novacodestudios.liminal.prensentation.library.LibraryScreen
import com.novacodestudios.liminal.prensentation.mangaReading.MangaReadingScreen
import com.novacodestudios.liminal.prensentation.novelReading.NovelReadingScreen
import com.novacodestudios.liminal.prensentation.screen.Screen
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import com.novacodestudios.liminal.util.encodeUrl
import com.novacodestudios.liminal.util.parcelableListType
import com.novacodestudios.liminal.util.parcelableType
import com.novacodestudios.liminal.util.withEncodedUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val items = listOf(
            BottomNavigationItem(
                title = "Ana Sayfa",
                selectedIcon = Icons.Filled.Home,
                unSelectedIcon = Icons.Outlined.Home,
                route = Screen.Home
            ),
            BottomNavigationItem(
                title = "Kütüphane",
                selectedIcon = Icons.Filled.CollectionsBookmark,
                unSelectedIcon = Icons.Outlined.CollectionsBookmark,
                route = Screen.Library
            ),
        )
        setContent {
            LiminalTheme {
                val navController = rememberNavController()
                //val navBackStackEntry by navController.currentBackStackEntryAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        bottomBar = {
                            /* val currentRoute = navBackStackEntry?.destination?.route
                             val shouldDisplayBottomBar = when (currentRoute) {
                                 "com.novacodestudios.liminal.prensentation.screen.Screen.Home",
                                 "com.novacodestudios.liminal.prensentation.screen.Screen.Library" -> true
                                 else -> false
                             }*/

                            BottomBar(navController = navController, navItems = items)


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

@Composable
fun BottomBar(navItems: List<BottomNavigationItem>, navController: NavHostController) {
    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }
    NavigationBar {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                label = { Text(text = item.title) },
                alwaysShowLabel = false,
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selectedItemIndex == index) {
                            item.selectedIcon
                        } else {
                            item.unSelectedIcon
                        },
                        contentDescription = item.title
                    )
                })
        }
    }
}


data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    val route: Screen
)


@Serializable
sealed class Graph {
    @Serializable
    data object Main : Graph()

    @Serializable
    data object Root : Graph()
}

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
                }
            )
        }
    }
}
