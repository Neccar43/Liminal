package com.novacodestudios.liminal.prensentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novacodestudios.liminal.domain.model.SeriesSummary
import com.novacodestudios.liminal.prensentation.component.LiminalProgressIndicator
import com.novacodestudios.liminal.prensentation.component.LiminalSearchBarWithAnimation
import com.novacodestudios.liminal.prensentation.home.component.HomeTopBar
import com.novacodestudios.liminal.prensentation.home.component.SeriesGridList
import com.novacodestudios.liminal.prensentation.home.component.SeriesListItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateDetailScreen: (SeriesSummary) -> Unit
) {
    val snackbarHostState =
        remember { SnackbarHostState() }

    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is HomeViewModel.UIEvent.Error -> snackbarHostState.showSnackbar(
                    event.error.asString(
                        context
                    )
                )
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
    onNavigateDetailScreen: (SeriesSummary) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var isActive by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = { HomeTopBar(scrollBehavior, onSearchClick = { isActive = true }) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(NavigationBarDefaults.windowInsets)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                val groupedSeriesList = state.seriesList.groupBy { it.source }
                items(groupedSeriesList.keys.toList(), key = { it }) { source ->
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

    LiminalSearchBarWithAnimation(
        active = isActive,
        onActiveChange = { isActive = it },
        query = state.query ?: "",
        onQueryChange = { onEvent(HomeEvent.OnQueryChanged(it)) },
        placeholderText = "Serilerde arayın",
    ) {
        LazyVerticalGrid(columns = GridCells.Fixed(3)) {
            items(state.searchSeries) { series ->
                SeriesListItem(
                    series = series,
                    onClick = { onNavigateDetailScreen(series) }
                )
            }
        }
    }

    LiminalProgressIndicator(modifier = Modifier.fillMaxSize(), isLoading = state.isLoading)
}