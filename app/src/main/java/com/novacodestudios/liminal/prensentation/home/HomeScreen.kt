package com.novacodestudios.liminal.prensentation.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.domain.model.NovelPreview
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.component.LiminalSearchBar
import com.novacodestudios.liminal.prensentation.home.component.HomeTopBar
import com.novacodestudios.liminal.prensentation.home.component.SeriesGridList
import com.novacodestudios.liminal.prensentation.home.component.SeriesListItem
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview as ComposPreview


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateDetailScreen: (SeriesPreview) -> Unit
) {
    val snackbarHostState =
        remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is HomeViewModel.UIEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    HomeContent(
        snackBarHostState = snackbarHostState,
        state = viewModel.state,
        onEvent = viewModel::onEvent,
        onNavigateDetailScreen = onNavigateDetailScreen
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    snackBarHostState: SnackbarHostState,
    onEvent: (HomeEvent) -> Unit,
    onNavigateDetailScreen: (SeriesPreview) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = { HomeTopBar(scrollBehavior) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            LiminalSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                query = state.query ?: "",
                onSearch = { onEvent(HomeEvent.OnQueryChanged(it)) }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(state.searchSeries) { series ->
                        SeriesListItem(
                            series = series,
                            onClick = { onNavigateDetailScreen(series) }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                val groupedSeriesList = state.seriesList.groupBy { it.source }
                items(groupedSeriesList.keys.toList()) { source ->
                    SeriesGridList(
                        modifier = Modifier.padding(top = 8.dp),
                        source = source,
                        seriesList = groupedSeriesList[source]!!,
                        onClick = { onNavigateDetailScreen(it) }
                    )
                }
            }
        }
    }

    LiminalProgressIndicator(modifier = Modifier.fillMaxSize(), isLoading = state.isLoading)
}


@ComposPreview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    LiminalTheme {
        HomeContent(state = HomeState(
            mangaList = listOf(
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisenjfkçjhfjhksdjhkfdjhşkjhlsdkfjhl",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "tempest"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "tempest"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                MangaPreview(
                    "Jujutsu Kaisen",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
            ),
            novelList = listOf(
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),
                NovelPreview(
                    "Classroom of the Elite",
                    "https://sadscans.com/assets/series/60953d39538a8/laaa.jpg",
                    "detailPageUrl",
                    "sadscans"
                ),


                ),
        ), onEvent = {}, onNavigateDetailScreen = {

        },
            snackBarHostState = SnackbarHostState()
        )

    }
}
