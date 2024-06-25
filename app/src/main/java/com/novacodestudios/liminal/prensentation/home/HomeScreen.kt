package com.novacodestudios.liminal.prensentation.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.R
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.domain.model.NovelPreview
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import com.novacodestudios.liminal.util.capitalizeFirstLetter
import androidx.compose.ui.tooling.preview.Preview as ComposPreview
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.prensentation.detail.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.library.LibraryViewModel
import kotlinx.coroutines.flow.collectLatest


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
        snackbarHost = { SnackbarHost(hostState = snackBarHostState)},
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

    LiminalProgressIndicator(modifier = Modifier.fillMaxSize(),isLoading = state.isLoading)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(title = { Text(text = "Liminal") }, scrollBehavior = scrollBehavior)
}

@Composable
fun SeriesGridList(
    modifier: Modifier = Modifier,
    source: String,
    seriesList: List<SeriesPreview>,
    onClick: (SeriesPreview) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = "${source.capitalizeFirstLetter()} Kaynağından",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(seriesList.chunked(2)) { chunk ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    chunk.forEach { series ->
                        SeriesListItem(
                            series = series,
                            onClick = { onClick(series) }
                        )
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SeriesListItem(
    series: SeriesPreview,
    onClick: (SeriesPreview) -> Unit,
    itemHeight: Dp = 180.dp,
    itemWidth: Dp = 120.dp
) {
    Column(
        modifier = Modifier
            .clickable(onClick = { onClick(series) })
            .padding(8.dp)
            .width(itemWidth)
    ) {

        GlideImage(
            model = series.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .height(itemHeight)
                .clip(shape = RoundedCornerShape(8.dp))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop
        )
        Text(
            text = series.name,
            modifier = Modifier
                .padding(8.dp),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiminalSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onSearch: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isActive by remember {
        mutableStateOf(false)
    }
    SearchBar(
        modifier = modifier,
        query = query,
        onQueryChange = { onSearch(it) },
        onSearch = { onSearch(it) },
        active = isActive,
        onActiveChange = { isActive = it },
        placeholder = { Text(text = "Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
        content = content
    )
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
