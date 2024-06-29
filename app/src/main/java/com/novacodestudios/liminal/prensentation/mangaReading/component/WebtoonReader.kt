package com.novacodestudios.liminal.prensentation.mangaReading.component

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.novacodestudios.liminal.prensentation.component.ChapterNavigationButton

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WebtoonReader(
    modifier: Modifier = Modifier,
    urls: List<String>,
    onLoadNextChapter: () -> Unit,
    onLoadPreviousChapter: () -> Unit,
) {
    LazyColumn(modifier = modifier) {
        item {
            ChapterNavigationButton(text = "Önceki Bölüm", onClick = { onLoadPreviousChapter() })
        }
        items(urls) { url ->
            GlideImage(
                modifier = Modifier,
                model = url,
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
        item {
            ChapterNavigationButton(text = "Sonraki Bölüm", onClick = { onLoadNextChapter() })
        }
    }
}