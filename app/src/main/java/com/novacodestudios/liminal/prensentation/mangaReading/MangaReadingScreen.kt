package com.novacodestudios.liminal.prensentation.mangaReading

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

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
    Scaffold(topBar = {
        AnimatedVisibility(visible = isBarVisible) {
            Box {
                MangaReadingTopBar(
                    chapterName = state.currentChapter.title,
                    onNavigateUp = onNavigateUp
                )
            }

        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.readingMode) {
                ReadingMode.WEBTOON -> {
                    WebtoonReader(modifier = Modifier.fillMaxSize(), urls = state.imageUrls)
                }

                ReadingMode.LEFT_TO_RIGHT -> {
                    MangaReader(
                        modifier = Modifier.fillMaxSize(),
                        urls = state.imageUrls.reversed(),
                        onLoadNextChapter = {},
                        onLoadPreviousChapter = {},
                        isBarVisible = isBarVisible,
                        onImageClick = { isBarVisible = !isBarVisible }
                    )
                }

                ReadingMode.RIGHT_TO_LEFT -> {
                    LaunchedEffect(state.imageUrls) {
                        Log.d(TAG, "MangaReadingContent: urls: ${state.imageUrls}")
                    }
                    MangaReader(
                        modifier = Modifier
                            .fillMaxSize(),
                        urls = state.imageUrls,
                        onLoadNextChapter = {
                            Log.d(TAG, "onLoadNextChapter: Çalıştı")
                            onEvent(MangaEvent.OnNextChapter)
                        },
                        onLoadPreviousChapter = {
                            Log.d(TAG, "onLoadPreviousChapter: Çalıştı")
                            onEvent(MangaEvent.OnPreviousChapter)
                        },
                        isBarVisible = isBarVisible,
                        onImageClick = { isBarVisible = !isBarVisible }
                    )
                }
            }
        }

    }
}

private const val TAG = "MangaReadingScreen"

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WebtoonReader(modifier: Modifier = Modifier, urls: List<String>) {
    LazyColumn(modifier = modifier) {
        items(urls) { url ->
            GlideImage(modifier = Modifier.fillMaxSize(), model = url, contentDescription = null)
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun MangaReader(
    modifier: Modifier = Modifier,
    urls: List<String>,
    onLoadNextChapter: () -> Unit,
    onLoadPreviousChapter: () -> Unit,
    isBarVisible: Boolean,
    onImageClick: () -> Unit,
) {
    val newUrls = remember { mutableStateListOf<String>() }
    var isNextChapter by remember {
        mutableStateOf(false)
    }
    var isPreviousChapter by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(urls) {
        newUrls.clear()
        newUrls.add("")
        newUrls.addAll(urls)
        newUrls.add("")
    }

    val pagerState = rememberPagerState(pageCount = { newUrls.size })
    val zoomState = rememberZoomState()

    LaunchedEffect(urls) {
        Log.d(TAG, "MangaReader: page count ${pagerState.pageCount} newsUrls size: ${newUrls.size}")
        pagerState.scrollToPage(1)
    }

    LaunchedEffect(isNextChapter) {
        Log.d(TAG, "MangaReader: isNext chapter çalıştı $isNextChapter")
        if (isNextChapter) {
            Log.d(TAG, "MangaReader: sıradaki bölümün ilk sayfasına geçilecek")
            pagerState.scrollToPage(1)
            isNextChapter = false
        }
    }
    LaunchedEffect(isPreviousChapter) {
        Log.d(TAG, "MangaReader: isPrevious chapter çalıştı $isPreviousChapter")
        if (isPreviousChapter) {
            Log.d(TAG, "MangaReader: önceki bölün bölümün son sayfasına geçilecek")
            pagerState.scrollToPage(newUrls.size - 2)
            pagerState.currentPage
            isPreviousChapter = false
        }
    }
    Box(modifier = modifier) {
        HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState) { page ->
            GlideImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImageClick() }
                    .zoomable(zoomState = zoomState),
                model = newUrls[page],
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        AnimatedVisibility(visible = isBarVisible,modifier = Modifier.align(Alignment.BottomCenter)) {
            PageIndicator(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .padding(8.dp),
                pagerState = pagerState
            )

        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
                .collect { (page, isScrollInProgress) ->
                    // Log.d(TAG, "MangaReader: pager state değişti")
                    if (page == newUrls.size - 1 && isScrollInProgress) {
                        onLoadNextChapter()
                        isNextChapter = true
                    }
                    if (page == 0 && isScrollInProgress) {
                        onLoadPreviousChapter()
                        isPreviousChapter = true
                    }
                }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageIndicator(modifier: Modifier = Modifier, pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    Card(modifier = modifier, shape = RoundedCornerShape(50.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val currentPage = pagerState.currentPage
            val totalPage = pagerState.pageCount - 2
            Text(text = (currentPage).toString())
            Spacer(modifier = Modifier.padding(8.dp))
            repeat(totalPage) {
                val iteration = it + 1
                val color =
                    if (currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(12.dp)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(iteration)
                            }
                        }
                )
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = (totalPage).toString())

        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaReadingTopBar(chapterName: String, onNavigateUp: () -> Unit) {
    TopAppBar(title = { Text(text = chapterName) }, navigationIcon = {
        IconButton(onClick = { onNavigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
    })
}

@Preview(showBackground = true)
@Composable
private fun Readerprew() {
    var isVisible by remember {
        mutableStateOf(true)
    }
    LiminalTheme {
        MangaReader(urls = listOf(
            "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/001.jpg",
            "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/002.jpg",
            "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/003.jpg",
            "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/004.jpg",
            "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/005.jpg",
            "https://sadscans.com/assets/series/60956a558cfbd/6096f0e57f331/images/006.jpg",
        ), onLoadNextChapter = { }, onLoadPreviousChapter = {

        },
            isBarVisible = isVisible,
            onImageClick = {isVisible=!isVisible}


        )

    }
}