package com.novacodestudios.liminal.prensentation.mangaReading

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.util.Log
import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.mangaReading.component.MangaReader
import com.novacodestudios.liminal.prensentation.mangaReading.component.MangaReadingTopBar
import com.novacodestudios.liminal.prensentation.mangaReading.component.ReadModeBottomSheet
import com.novacodestudios.liminal.prensentation.mangaReading.component.WebtoonReader
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.flow.collectLatest

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

// TODO: chpterdaki kalan sayfa sayısını göster
// TODO: kullanıcı bir manga paneline basılı tuttuğunda bir menü göster (kaydet paylaş vs)
@Composable
fun MangaReadingScreen(
    viewModel: MangaReadingViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    //implementation "com.google.accompanist:accompanist-systemuicontroller:0.28.0"
    val snackbarHostState =
        remember { SnackbarHostState() }

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is MangaReadingViewModel.UIEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }



    MangaReadingContent(
        state = viewModel.state,
        onEvent = viewModel::onEvent,
        snackBarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
    )
}

@Composable
fun MangaReadingContent(
    state: MangaState,
    onEvent: (MangaEvent) -> Unit,
    snackBarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
) {
    var isBarVisible by remember {
        mutableStateOf(true)
    }
    var isSheetVisible by remember {
        mutableStateOf(false)
    }
    var isChapterListVisible by remember {
        mutableStateOf(false)
    }
    val context=LocalContext.current
    val window = context.findActivity()?.window ?: return
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    LaunchedEffect(isBarVisible) {
        if (isBarVisible){
            showSystemBars(insetsController)
            return@LaunchedEffect
        }
        hideSystemBars(insetsController)
    }


    DisposableEffect(Unit) {
        onDispose {
            showSystemBars(insetsController)
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = isBarVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
            ) {
                MangaReadingTopBar(
                    mangaName = state.series?.name ?: "",
                    chapterName = state.currentChapter.title,
                    onNavigateUp = onNavigateUp,
                    onClickReadModeIcon = { isSheetVisible = true },
                    onClickChapterListIcon = { isChapterListVisible = true }
                )
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
                            pageIndex = state.series?.currentPageIndex ?: 0,
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
                            pageIndex = state.series?.currentPageIndex ?: 0,
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
                        onItemClick = {
                            onEvent(MangaEvent.OnReadingModeChanged(it))
                            isChapterListVisible = false
                        }
                    )
                }

                if (isChapterListVisible) {
                    ChapterListDialog(
                        chapterList = state.chapters.reversed(),
                        currentChapter = state.currentChapter,
                        onDismiss = { isChapterListVisible = false },
                        onItemClick = { onEvent(MangaEvent.OnChapterChange(it)) }
                    )
                }


            }

            LiminalProgressIndicator(modifier = Modifier.fillMaxSize(), isLoading = state.isLoading)

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListDialog(
    chapterList: List<Chapter>,
    currentChapter: Chapter,
    onDismiss: () -> Unit,
    onItemClick: (Chapter) -> Unit
) {
    val scrollState = rememberLazyListState()

    val currentChapterIndex = chapterList.indexOf(currentChapter)

    LaunchedEffect(currentChapterIndex) {
        if (currentChapterIndex >= 0) {
            // scrollState.animateScrollToItem(currentChapterIndex)
            scrollState.scrollToItem(currentChapterIndex)
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .background(AlertDialogDefaults.containerColor, shape = AlertDialogDefaults.shape)
    ) {
        LazyColumn(
            modifier = Modifier.padding(vertical = 16.dp),
            state = scrollState
        ) {
            items(chapterList.size) { index ->
                ChapterListItem(
                    chapter = chapterList[index],
                    onItemClick = { onItemClick(it); onDismiss() },
                    isSelected = chapterList[index] == currentChapter
                )
            }
        }
    }
}


@Composable
fun ChapterListItem(chapter: Chapter, onItemClick: (Chapter) -> Unit, isSelected: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(chapter) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
            fontWeight = if (isSelected) FontWeight.Bold else null
        )
        Text(
            text = chapter.releaseDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else null
        )
    }
}

private fun showSystemBars(insetsController: WindowInsetsControllerCompat) {
    insetsController.apply {
        show(WindowInsetsCompat.Type.statusBars())
        show(WindowInsetsCompat.Type.navigationBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

private fun hideSystemBars(insetsController: WindowInsetsControllerCompat) {
    insetsController.apply {
        hide(WindowInsetsCompat.Type.statusBars())
        hide(WindowInsetsCompat.Type.navigationBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}



private const val TAG = "MangaReadingScreen"

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Readerprew() {
    var isVisible by remember {
        mutableStateOf(false)
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