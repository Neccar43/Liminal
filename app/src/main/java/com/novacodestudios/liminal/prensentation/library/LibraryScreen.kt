package com.novacodestudios.liminal.prensentation.library

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.R
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.theme.LiminalTheme
import com.novacodestudios.liminal.util.formatTimeAgo
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
                        onDownload = {},
                        onClick = {
                            onEvent(LibraryEvent.OnSeriesItemClicked(seriesEntity))
                        })
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
@Composable
fun EmptyStateMessage(
    modifier: Modifier = Modifier,
    message: String,
    icon: ImageVector = Icons.Default.Info,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    messageStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    iconSize: Dp = 64.dp,
    spacing: Dp = 16.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(spacing)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(spacing))
            Text(
                text = message,
                style = messageStyle,
                textAlign = TextAlign.Center
            )
        }
    }
}



@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SeriesItem(
    modifier: Modifier = Modifier,
    series: SeriesEntity,
    onClick: () -> Unit,
    onReset: (SeriesEntity) -> Unit,
    onDownload: (SeriesEntity) -> Unit
) {
    //val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(
                onClick = { onClick() },
            )
    ) {
        GlideImage(
            model = series.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(70.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            Text(
                text = series.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // TODO: Bunu sonradan ekle
            // Text(text = "Current Chapter name", style = MaterialTheme.typography.subtitle2)

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "En son okuma " + formatTimeAgo(series.lastReadingDateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(
                        onClick = { onReset(series) },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.restart_alt_24dp),
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onDownload(series) },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DownloadForOffline,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
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
        ){
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
