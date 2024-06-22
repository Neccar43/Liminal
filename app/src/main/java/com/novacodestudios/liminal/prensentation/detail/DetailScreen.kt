package com.novacodestudios.liminal.prensentation.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
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
import com.bumptech.glide.integration.compose.placeholder
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.MangaDetail
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onNavigateMangaReadingScreen: (Chapter,List<Chapter>) -> Unit,
    onNavigateNovelReadingScreen: (Chapter,List<Chapter>) -> Unit
) {
    DetailContent(
        state = viewModel.state,
        onNavigateUp = onNavigateUp,
        onNavigateMangaReadingScreen = onNavigateMangaReadingScreen,
        onNavigateNovelReadingScreen = onNavigateNovelReadingScreen
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun DetailContent(
    state: DetailState,
    onNavigateUp: () -> Unit,
    onNavigateMangaReadingScreen: (Chapter,List<Chapter>) -> Unit,
    onNavigateNovelReadingScreen: (Chapter,List<Chapter>) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(topBar = {
        DetailTopBar(scrollBehavior = scrollBehavior, onNavigateUp = onNavigateUp)
    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) { paddingValues ->
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
                text = state.detail?.summary ?: "null",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val firstChapter= if(state.detail?.chapters?.isEmpty() == true) null else state.detail?.chapters?.first()

            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), onClick = {

                when (state.detail!!.type) {
                    SeriesType.MANGA -> onNavigateMangaReadingScreen(firstChapter!!,state.detail.chapters)
                    SeriesType.NOVEL -> onNavigateNovelReadingScreen(firstChapter!!,state.detail.chapters)
                }
            }) {
                Text(text = "Okumaya ${firstChapter?.title}'den Başla")
            }
            //Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bölümler",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))


            (state.detail?.chapters ?: emptyList()).forEach { chapter ->
                ChapterItem(
                    chapter = chapter, onChapterClick ={
                        when (state.detail!!.type) {
                            SeriesType.MANGA -> onNavigateMangaReadingScreen(chapter,state.detail.chapters)
                            SeriesType.NOVEL -> onNavigateNovelReadingScreen(chapter,state.detail.chapters)
                        }
                    }

                )
            }
        }
        LiminalProgressIndicator(Modifier.fillMaxSize(), isLoading = state.isLoading)
    }
}

@Composable
fun LiminalProgressIndicator(modifier: Modifier = Modifier, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier)
        }
    }
}

@Composable
fun ChapterItem(
    modifier: Modifier = Modifier,
    chapter: Chapter,
    onChapterClick: (Chapter) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onChapterClick(chapter) },
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = chapter.releaseDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(scrollBehavior: TopAppBarScrollBehavior, onNavigateUp: () -> Unit) {
    TopAppBar(
        modifier = Modifier,
        title = { Text(text = "Detail") },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = { onNavigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        })
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
            onNavigateMangaReadingScreen = {_,_->

            },
            onNavigateNovelReadingScreen = {_,_->

            }
        )
    }

}