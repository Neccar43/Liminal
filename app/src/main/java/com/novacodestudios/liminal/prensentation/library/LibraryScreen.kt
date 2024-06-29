package com.novacodestudios.liminal.prensentation.library

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.component.EmptyStateMessage
import com.novacodestudios.liminal.prensentation.library.component.SeriesItem
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateMangaReadingScreen: (Chapter, List<Chapter>) -> Unit,
    onNavigateNovelReadingScreen: (Chapter, String) -> Unit
) {
    val snackbarHostState =
        remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LibraryViewModel.UIState.ShowSnackBar -> snackbarHostState.showSnackbar(event.message)
                is LibraryViewModel.UIState.NavigateReadingScreen -> {
                    if (event.seriesEntity.isManga) {
                        onNavigateMangaReadingScreen(
                            viewModel.state.selectedChapter!!,
                            viewModel.state.selectedChapterList
                        )
                        return@collectLatest
                    }
                    onNavigateNovelReadingScreen(
                        viewModel.state.selectedChapter!!,
                        event.seriesEntity.detailPageUrl
                    )
                }
            }
        }
    }
    LibraryScreenContent(
        state = viewModel.state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun LibraryScreenContent(
    state: LibraryState,
    snackbarHostState: SnackbarHostState,
    onEvent: (LibraryEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {}
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.seriesEntityList) { seriesEntity ->
                    SeriesItem(
                        modifier = Modifier.fillMaxWidth(),
                        series = seriesEntity,
                        onReset = { onEvent(LibraryEvent.OnResetSeries(it)) },
                        onDownload = {}, // TODO: İndirme özelliğini ekle
                        onClick = {
                            onEvent(LibraryEvent.OnSeriesItemClicked(seriesEntity))
                        }
                    )
                }
            }
        }

        if (state.seriesEntityList.isEmpty()) {
            EmptyStateMessage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                message = "Kütüphaneniz şu anda boş görünüyor. Ana ekrandan okuduğunuz içerikler otomatik olarak buraya eklenecektir."
            )
            return@Scaffold
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun emtymessage() {
    LiminalTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            EmptyStateMessage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                message = "Kütüphaneniz şu anda boş görünüyor. Ana ekrandan okuduğunuz içerikler otomatik olarak buraya eklenecektir."
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Item() {
    LiminalTheme {
        LibraryScreenContent(state = LibraryState(
            seriesEntityList = listOf(
                SeriesEntity(
                    id = "",
                    name = "Jujutsu Kaisen",
                    lastReadingDateTime = System.currentTimeMillis(),
                    detailPageUrl = "",
                    imageUrl = "",
                    currentChapterId = "",
                    currentPageIndex = 0,
                    isManga = true,
                ),
                SeriesEntity(
                    id = "",
                    name = "Jujutsu Kaisen",
                    lastReadingDateTime = System.currentTimeMillis(),
                    detailPageUrl = "",
                    imageUrl = "",
                    currentChapterId = "",
                    currentPageIndex = 0,
                    isManga = true,
                ),
                SeriesEntity(
                    id = "",
                    name = "Jujutsu Kaisen",
                    lastReadingDateTime = System.currentTimeMillis(),
                    detailPageUrl = "",
                    imageUrl = "",
                    currentChapterId = "",
                    currentPageIndex = 0,
                    isManga = true,
                ),
                SeriesEntity(
                    id = "",
                    name = "Jujutsu Kaisen",
                    lastReadingDateTime = System.currentTimeMillis(),
                    detailPageUrl = "",
                    imageUrl = "",
                    currentChapterId = "",
                    currentPageIndex = 0,
                    isManga = true,
                ),

                )
        ), snackbarHostState = remember { SnackbarHostState() }, onEvent = {

        }


        )
    }
}
