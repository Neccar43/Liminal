package com.novacodestudios.liminal.prensentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.MangaDetail
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.detail.component.ChapterItem
import com.novacodestudios.liminal.prensentation.detail.component.DetailTopBar
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateMangaReadingScreen: (Chapter, List<Chapter>) -> Unit,
    onNavigateNovelReadingScreen: (Chapter, String) -> Unit
) {
    val snackbarHostState =
        remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DetailViewModel.UIState.ShowSnackBar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    DetailContent(
        state = viewModel.state,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onNavigateMangaReadingScreen = onNavigateMangaReadingScreen,
        onNavigateNovelReadingScreen = onNavigateNovelReadingScreen,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun DetailContent(
    state: DetailState,
    snackbarHostState: SnackbarHostState,
    onEvent: (DetailEvent) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateMangaReadingScreen: (Chapter, List<Chapter>) -> Unit,
    onNavigateNovelReadingScreen: (Chapter, String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(scrollBehavior = scrollBehavior, onNavigateUp = onNavigateUp)
        }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                GlideImage(
                    model = state.detail?.imageUrl,
                    contentDescription = state.detail?.name,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                ),
                                startY = 300f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.detail?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        // color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.detail?.author ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        // color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.detail?.summary ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val firstChapter =
                if (state.chapterList.isEmpty()) null else state.chapterList.last()

            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), onClick = {
                if (state.chapterListError != null) {
                    onEvent(DetailEvent.OnChapterListLoadRetry)
                    return@Button
                }

                onEvent(DetailEvent.OnSeriesChapterClick(firstChapter!!))
                when (state.detail!!.type) {
                    SeriesType.MANGA -> onNavigateMangaReadingScreen(
                        firstChapter,
                        state.chapterList
                    )

                    SeriesType.NOVEL -> onNavigateNovelReadingScreen(
                        firstChapter,
                        state.detailPageUrl
                    )
                }
            }) {
                if (state.isChapterListLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    return@Button
                }
                if (state.chapterListError != null) {
                    Text(text = "Yeniden Deneyin")
                    return@Button
                }
                Text(text = "Okumaya ${firstChapter?.title}'den Başla")
            }
            //Spacer(modifier = Modifier.height(16.dp))


            if (state.isChapterListLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            } else {
                Text(
                    text = "Bölümler",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.chapterList.forEach { chapter ->
                    ChapterItem(
                        chapter = chapter, onChapterClick = {
                            onEvent(DetailEvent.OnSeriesChapterClick(chapter))
                            when (state.detail!!.type) {
                                SeriesType.MANGA -> onNavigateMangaReadingScreen(
                                    chapter,
                                    state.chapterList
                                )

                                SeriesType.NOVEL -> onNavigateNovelReadingScreen(
                                    chapter,
                                    state.detailPageUrl
                                )
                            }
                        }

                    )
                }
            }

        }
        LiminalProgressIndicator(Modifier.fillMaxSize(), isLoading = state.isLoading)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailPreview() {
    LiminalTheme {
        DetailContent(
            state = DetailState(
                isLoading = true,
                detail = MangaDetail(
                    name = "Manga Name",
                    imageUrl = "https://via.placeholder.com/150",
                    author = "Author",
                    summary = "Summary",
                    chapters = listOf(
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                        Chapter(
                            title = "Chapter 1",
                            releaseDate = "2021-01-01",
                            url = ""
                        ),
                    ),
                    type = SeriesType.MANGA
                ),
                type = SeriesType.MANGA,
                detailPageUrl = ""
            ),
            onNavigateUp = { },
            onNavigateMangaReadingScreen = { _, _ ->

            },
            onNavigateNovelReadingScreen = { _, _ ->

            },
            onEvent = {},
            snackbarHostState = SnackbarHostState()
        )
    }

}