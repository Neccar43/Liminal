package com.novacodestudios.liminal

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.SeriesDetail
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.prensentation.detail.DetailScreen
import com.novacodestudios.liminal.prensentation.home.HomeScreen
import com.novacodestudios.liminal.prensentation.mangaReading.MangaReadingScreen
import com.novacodestudios.liminal.prensentation.novelReading.NovelReadingScreen
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import com.novacodestudios.liminal.util.encodeUrl
import com.novacodestudios.liminal.util.parcelableListType
import com.novacodestudios.liminal.util.parcelableType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.reflect.typeOf

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiminalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = HomeScreen) {
                        composable<HomeScreen> {
                            HomeScreen(onNavigateDetailScreen = { preview ->
                                navController.navigate(
                                    DetailScreen(
                                        preview.detailPageUrl,
                                        preview.type
                                    )
                                )
                            })
                        }

                        composable<DetailScreen>(
                            typeMap = mapOf(typeOf<SeriesType>() to parcelableType<SeriesType>())
                        ) {
                            DetailScreen(
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateMangaReadingScreen = { chapter,chapters ->
                                    Log.d(TAG, "onNavigateMangaReadingScreen: çalıştı $chapter")
                                    navController.navigate(
                                        MangaReadingScreen(
                                            currentChapter = chapter.withEncodedUrl(),
                                            chapters = chapters.map { it.withEncodedUrl() },
                                        )
                                    )
                                },
                                onNavigateNovelReadingScreen = { chapter,chapters ->
                                    navController.navigate(
                                        NovelReadingScreen(
                                           chapters = chapters.map { it.withEncodedUrl() },
                                            currentChapter = chapter.withEncodedUrl(),
                                        )
                                    )
                                },
                            )
                        }

                        composable<MangaReadingScreen>(
                            typeMap = mapOf(typeOf<List<Chapter>>() to parcelableListType<Chapter>(),
                                typeOf<Chapter>() to parcelableType<Chapter>()
                            )
                        ) {
                            MangaReadingScreen(onNavigateUp = {
                                navController.navigateUp()
                            })
                        }
                        composable<NovelReadingScreen>(
                            typeMap = mapOf(typeOf<List<Chapter>>() to parcelableListType<Chapter>(),
                                typeOf<Chapter>() to parcelableType<Chapter>()
                            )
                        ) {
                            NovelReadingScreen(onNavigateUp = {

                            })
                        }

                        composable<LibraryScreen>(
                        ) {

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


@Serializable
object HomeScreen

@Serializable
data class DetailScreen(val detailPageUrl: String, val type: SeriesType)

// TODO: Burada sadece chapter nesenesi al
@Serializable
data class NovelReadingScreen(val chapters:List<Chapter>, val currentChapter: Chapter)

@Serializable
data class MangaReadingScreen(val chapters:List<Chapter>, val currentChapter: Chapter)

@Serializable
object LibraryScreen





fun Chapter.withEncodedUrl():Chapter= this.copy(url = this.url.encodeUrl())





