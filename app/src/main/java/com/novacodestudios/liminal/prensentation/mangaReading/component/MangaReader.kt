package com.novacodestudios.liminal.prensentation.mangaReading.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MangaReader(
    modifier: Modifier = Modifier,
    urls: List<Any>,
    pageIndex: Int,
    onLoadNextChapter: () -> Unit,
    onLoadPreviousChapter: () -> Unit,
    isBarVisible: Boolean,
    onImageClick: () -> Unit,
    onPageChange: (page: Int) -> Unit,
) {
    if (urls.isEmpty()) return

    var newUrls by remember { mutableStateOf(listOf<Any>()) }
    var isNextChapter by remember {
        mutableStateOf(false)
    }
    var isPreviousChapter by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(urls) {
        Log.d(TAG, "MangaReader: -----------------------------------------------------------------------------")
        newUrls= listOf("")+urls+listOf("")
        Log.d(TAG, "MangaReader: newUrl ayarlandı")
    }

    val pagerState = rememberPagerState(pageCount = { newUrls.size })
    val zoomState = rememberZoomState()


    LaunchedEffect(isNextChapter) {
        Log.d(TAG, "MangaReader: is next chapter çalıştı")
        if (isNextChapter) {
            Log.d(TAG, "MangaReader: isNextChapter true 1. sayfaya scroll edildi")
            pagerState.scrollToPage(1)
            isNextChapter = false
        }
    }
    LaunchedEffect(key1 = isPreviousChapter) {
        Log.d(TAG, "MangaReader: isPreviousChapter çalıştı")
        if (isPreviousChapter) {
            Log.d(TAG, "MangaReader: isPreviousChapter true ${urls.size}. sayfaya scroll edildi")
            pagerState.scrollToPage(urls.size)
            isPreviousChapter = false
        }
    }
    LaunchedEffect(Unit) {
        Log.d(TAG, "MangaReader: initial scroll çalıştı ${pageIndex + 1}. sayfaya scroll edildi")
        pagerState.scrollToPage(pageIndex + 1)
    }
    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            GlideImage(
                model = newUrls[page],
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImageClick() }
                    .zoomable(zoomState)
            )
        }
        if (isBarVisible) {
            PageIndicator(
                pagerState = pagerState,
                pageCount = urls.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
                .collect { (page, isScrollInProgress) ->
                    if (page == newUrls.size - 1 && isScrollInProgress) {
                        onLoadNextChapter()
                        isNextChapter = true
                    }
                    if (page == 0 && isScrollInProgress) {
                        onLoadPreviousChapter()
                        isPreviousChapter = true
                    }

                }
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onPageChange(page - 1)
            }
        }
    }
}

@Composable
fun PageIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = modifier, shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = pageCount.toString())
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = (pageCount - pagerState.currentPage).toFloat(),
                onValueChange = { newValue ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(pageCount - newValue.roundToInt())
                    }
                },
                valueRange = 0f..(pageCount - 1).toFloat(),
                steps = pageCount - 2,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.inversePrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = pagerState.currentPage.toString())
        }
    }
}

private const val TAG = "MangaReader"