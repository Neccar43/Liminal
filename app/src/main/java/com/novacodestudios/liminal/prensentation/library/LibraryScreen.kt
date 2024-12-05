package com.novacodestudios.liminal.prensentation.library

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.prensentation.component.EmptyStateMessage
import com.novacodestudios.liminal.prensentation.library.component.SeriesItem
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateMangaReadingScreen: (chapterId: String) -> Unit,
    onNavigateNovelReadingScreen: (chapterId: String) -> Unit
) {
    val snackbarHostState =
        remember { SnackbarHostState() }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LibraryViewModel.UIState.ShowSnackBar ->
                    snackbarHostState.showSnackbar(event.message.asString(context))

                is LibraryViewModel.UIState.NavigateReading -> {
                    when (event.seriesType) {
                        SeriesType.MANGA -> {
                            onNavigateMangaReadingScreen(event.seriesId)
                        }

                        SeriesType.NOVEL -> {
                            onNavigateNovelReadingScreen(event.seriesId)
                        }
                    }
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
        topBar = {},
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(NavigationBarDefaults.windowInsets)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.seriesList) { series ->
                    SeriesItem(
                        modifier = Modifier.fillMaxWidth(),
                        series = series,
                        onReset = {
                            Log.d(TAG, "onReset çalıştı")
                            onEvent(LibraryEvent.OnResetSeries(it))
                        },
                        onDownload = { onEvent(LibraryEvent.OnDownloadSeries(it)) },
                        onClick = {
                            Log.d(TAG, "onClick çalıştı")
                            onEvent(LibraryEvent.OnSeriesItemClicked(series))
                        }
                    )
                }
            }
        }

        if (state.seriesList.isEmpty()) {
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
            seriesList = listOf()
        ), snackbarHostState = remember { SnackbarHostState() }, onEvent = {

        }


        )
    }
}

private const val TAG = "LibraryScreen"
