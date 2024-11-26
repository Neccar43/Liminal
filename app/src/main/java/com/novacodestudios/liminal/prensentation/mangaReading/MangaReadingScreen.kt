package com.novacodestudios.liminal.prensentation.mangaReading

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.mangaReading.component.MangaReader
import com.novacodestudios.liminal.prensentation.mangaReading.component.MangaReadingTopBar
import com.novacodestudios.liminal.prensentation.mangaReading.component.ReadModeBottomSheet
import com.novacodestudios.liminal.prensentation.mangaReading.component.WebtoonReader
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme

@Composable
fun MangaReadingScreen(
    viewModel: MangaReaderViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    MangaReadingContent(
        state = viewModel.state,
        onEvent = viewModel::onEvent,
        onNavigateUp = onNavigateUp,
    )
}

@Composable
fun MangaReadingContent(
    state: MangaState,
    onEvent: (MangaEvent) -> Unit,
    onNavigateUp: () -> Unit,
) {
    var isBarVisible by remember {
        mutableStateOf(true)
    }
    var isSheetVisible by remember {
        mutableStateOf(false)
    }
    Scaffold(topBar = {
        AnimatedVisibility(visible = isBarVisible) {
            Box {
                MangaReadingTopBar(
                    mangaName = state.seriesEntity?.name ?: "",
                    chapterName = state.currentChapter!!.title,
                    onNavigateUp = onNavigateUp,
                    onClickReadModeBIcon = { isSheetVisible = true }
                )
            }
        }
    },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(NavigationBarDefaults.windowInsets),

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LaunchedEffect(state.imageSources) {
                Log.d(TAG, "MangaReadingContent: urls: ${state.imageSources}")
            }
            if (!state.isLoading) {
                when (state.readingMode) {
                    ReadingMode.WEBTOON -> {
                        WebtoonReader(
                            modifier = Modifier
                                .fillMaxSize(),
                            urls = state.imageSources,
                            onLoadNextChapter = {
                                Log.d(TAG, "WebtoonReader: sonraki sayfa yüklenecek")
                                onEvent(MangaEvent.OnNextChapter)
                            },
                            onLoadPreviousChapter = {
                                Log.d(TAG, "WebtoonReader: önceki sayfa yüklenecek")
                                onEvent(MangaEvent.OnPreviousChapter)
                            },
                        )

                    }

                    ReadingMode.LEFT_TO_RIGHT -> {
                        MangaReader(
                            modifier = Modifier
                                .fillMaxSize(),
                            urls = state.imageSources,
                            pageIndex = state.seriesEntity?.currentPageIndex ?: 0,
                            onLoadNextChapter = {
                                Log.d(TAG, "onLoadNextChapter: Çalıştı")
                                onEvent(MangaEvent.OnNextChapter)
                            },
                            onLoadPreviousChapter = {
                                Log.d(TAG, "onLoadPreviousChapter: Çalıştı")
                                onEvent(MangaEvent.OnPreviousChapter)
                            },
                            isBarVisible = isBarVisible,
                            onImageClick = { isBarVisible = !isBarVisible },
                            onPageChange = {
                                Log.d(TAG, "onPageChange: $it")
                                onEvent(MangaEvent.OnPageChange(pageIndex = it))
                            }
                        )
                    }

                    ReadingMode.RIGHT_TO_LEFT -> {
                        MangaReader(
                            modifier = Modifier
                                .fillMaxSize(),
                            urls = state.imageSources.reversed(), // TODO: parametre olarak read mode al ve inducatörün yönünğde değştir buradaki when i kaldır
                            pageIndex = state.seriesEntity?.currentPageIndex ?: 0,
                            onLoadNextChapter = {
                                Log.d(TAG, "onLoadNextChapter: Çalıştı")
                                onEvent(MangaEvent.OnNextChapter)
                            },
                            onLoadPreviousChapter = {
                                Log.d(TAG, "onLoadPreviousChapter: Çalıştı")
                                onEvent(MangaEvent.OnPreviousChapter)
                            },
                            isBarVisible = isBarVisible,
                            onImageClick = { isBarVisible = !isBarVisible },
                            onPageChange = {
                                Log.d(TAG, "onPageChange: $it")
                                onEvent(MangaEvent.OnPageChange(pageIndex = it))
                            }
                        )
                    }
                }

                if (isSheetVisible) {
                    ReadModeBottomSheet(
                        modifier = Modifier,
                        onDismiss = { isSheetVisible = false },
                        onItemClick = { onEvent(MangaEvent.OnReadingModeChanged(it)) }
                    )
                }
            }

            LiminalProgressIndicator(modifier = Modifier.fillMaxSize(), isLoading = state.isLoading)

        }

    }

}


private const val TAG = "MangaReadingScreen"

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Readerprew() {
    var isVisible by remember {
        mutableStateOf(true)
    }
    LiminalTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MangaReader(
                urls = listOf(
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/001.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/002.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/003.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/004.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/005.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/006.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/001.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/002.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/003.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/004.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/005.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/006.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/006.jpg",
                    "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/006.jpg",
                ),
                onLoadNextChapter = { },
                onLoadPreviousChapter = {

                },
                isBarVisible = isVisible,
                onImageClick = { isVisible = !isVisible },
                onPageChange = {},
                pageIndex = 5,

                )
        }


    }
}