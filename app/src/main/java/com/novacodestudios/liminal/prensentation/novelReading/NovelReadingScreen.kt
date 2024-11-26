package com.novacodestudios.liminal.prensentation.novelReading

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.component.ChapterNavigationButton
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.component.RetryStateMessage
import com.novacodestudios.liminal.prensentation.novelReading.component.NovelTopBar
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme

@Composable
fun NovelReadingScreen(
    viewModel: NovelReadingViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,

    ) {
    NovelReadingContent(
        state = viewModel.state,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NovelReadingContent(
    state: NovelReadingState,
    onEvent: (NovelEvent) -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            val series = state.seriesEntity
            if (series != null) {
                NovelTopBar(
                    onNavigateUp = onNavigateUp,
                    novelName = series.name,
                    chapterTitle = series.currentChapterName!!
                )
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(NavigationBarDefaults.windowInsets),
    ) { innerPadding ->
        if (!state.isLoading && state.error == null) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                ChapterNavigationButton(text = "Önceki Bölüm", onClick = {
                    onEvent(NovelEvent.OnPreviousChapter)
                })
                Spacer(modifier = Modifier.height(16.dp))
                state.chapterContent.forEach { paragraph ->
                    if (paragraph.isNotBlank()) {
                        if (paragraph.contains("https")) {
                            GlideImage(
                                modifier = Modifier.padding(vertical = 8.dp),
                                model = paragraph,
                                contentDescription = null
                            )
                        } else {
                            Text(
                                text = paragraph.trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ChapterNavigationButton(text = "Sonraki Bölüm", onClick = {
                    onEvent(NovelEvent.OnNextChapter)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
            return@Scaffold
        }

        LiminalProgressIndicator(modifier = Modifier.fillMaxSize(), isLoading = state.isLoading)

        if (state.error != null) {
            RetryStateMessage(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
                errorMessage = state.error,
                onClick = { onEvent(NovelEvent.OnContentRetry) })
            return@Scaffold
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LiminalTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NovelReadingContent(state = NovelReadingState(
                currentChapter = Chapter("", "", ""),
                detailPageUrl = "",
                error = "İçerik yüklemedi. Lütfen tekrar deneyin.",
                isLoading = false,
            ),
                onEvent = {},
                onNavigateUp = {

                })
        }
    }
}

