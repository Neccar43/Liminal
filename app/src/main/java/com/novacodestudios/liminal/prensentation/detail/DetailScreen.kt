package com.novacodestudios.liminal.prensentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.model.Tag
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.detail.component.ChapterItem
import com.novacodestudios.liminal.prensentation.detail.component.DetailTopBar
import com.novacodestudios.liminal.prensentation.detail.component.ErrorMessageBox
import com.novacodestudios.liminal.prensentation.detail.component.TagChip
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.flow.collectLatest

// TODO: Pading leri standart hale getir
// TODO: yazı boyutlandırmalrını yeniden ayarla
// TODO: paylaş butonu ekle
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateMangaReadingScreen: (chapterId: String) -> Unit,
    onNavigateNovelReadingScreen: (chapterId: String) -> Unit
) {
    val snackbarHostState =
        remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DetailViewModel.UIEvent.Error -> snackbarHostState.showSnackbar(
                    event.error.asString(
                        context
                    )
                )

                is DetailViewModel.UIEvent.NavigateReading -> {
                    viewModel.state.series?.type?.let { seriesType ->
                        when (seriesType) {
                            SeriesType.MANGA -> {
                                onNavigateMangaReadingScreen(event.chapterId)
                            }

                            SeriesType.NOVEL -> {
                                onNavigateNovelReadingScreen(event.chapterId)
                            }
                        }
                    }
                }
            }
        }
    }

    DetailContent(
        state = viewModel.state,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun DetailContent(
    state: DetailState,
    snackbarHostState: SnackbarHostState,
    onEvent: (DetailEvent) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(scrollBehavior = scrollBehavior, onNavigateUp = onNavigateUp)
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(NavigationBarDefaults.windowInsets),

        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
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
                    model = state.series?.imageUrl,
                    contentDescription = state.series?.name,
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
                        text = state.series?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        // color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.series?.author ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        // color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.series?.status ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        // color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.series?.summary ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (state.series?.tags?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Etiketler",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.series.tags.forEach { tag ->
                        TagChip(
                            modifier = Modifier,
                            tag = tag,
                            onTagClick = {} // TODO: Özel bir ekrana yönlendir arama ekranına yönledir
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            val firstChapter =
                if (state.chapterList.isEmpty()) null else state.chapterList.last()

            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), onClick = {
                if (state.chapterListError) {
                    onEvent(DetailEvent.OnChapterListLoadRetry)
                    return@Button
                }

                onEvent(DetailEvent.OnSeriesChapterClick(firstChapter!!))
            }) {
                if (state.isChaptersLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    return@Button
                }
                if (state.chapterListError) {
                    Text(text = "Yeniden Deneyin")
                    return@Button
                }
                Text(text = "Okumaya ${firstChapter?.title}'den Başla")
            }
            //Spacer(modifier = Modifier.height(16.dp))

            if (state.chapterListError) {
                ErrorMessageBox(
                    errorMessage = "Bölümler yüklenirken bir hata oluştu."
                )

            }

            if (!state.isChaptersLoading && !state.chapterListError) {
                Text(
                    text = "Bölümler",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "${state.chapterList.size} bölüm",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.chapterList.forEach { chapter ->
                    ChapterItem(
                        chapter = chapter, onChapterClick = {
                            onEvent(DetailEvent.OnSeriesChapterClick(chapter))
                        },
                        onDownload = {}, // TODO: Chapter bazlı indirme işlemini ekle
                        onDelete = {}

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
    val summary =
        "Yuuji bir atletizm dehasıdır ama koşuya hiç ilgisi yoktur. " +
                "Bilinmezleri Araştırma Kulübünde çok mutludur. Her ne kadar kulüpte eğlencesine bulunsa bile, okulda gerçek bir ruh ortaya çıkınca işler ciddiye biner! " +
                "Hayat, Sugisawa Şehrindeki 3 Numaralı Lisedeki çocuklar için tuhaflaşmaya başlamak üzere! Not: 0. cildi (0 ile başlayan bölümler) 91. bölümden sonra okumanızı tavsiye ederiz."

    LiminalTheme {
        DetailContent(
            state = DetailState(
                isLoading = false,
                chapterListError = true,
                series = Series(
                    name = "Manga Name",
                    imageUrl = "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    author = "Author",
                    chapters = emptyList(),
                    type = SeriesType.MANGA,
                    summary = "summary",
                    source = Source.TEMPEST,
                    tags = listOf(
                        Tag("Seinen", ""),
                        Tag("Aksiyon", ""),
                        Tag("Drama", ""),
                    ),
                    status = "Güncel",
                    id = "",
                    detailPageUrl = "",
                    currentPageIndex = 0,
                    currentChapterName = "",
                    lastReadingDateTime = 0,
                    currentChapterId = "",
                ),
                chapterList = emptyList(),
                detailPageUrl = ""
            ),
            onNavigateUp = { },
            onEvent = {},
            snackbarHostState = SnackbarHostState()
        )
    }

}