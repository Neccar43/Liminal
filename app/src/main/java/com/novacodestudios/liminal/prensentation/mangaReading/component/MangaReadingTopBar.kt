package com.novacodestudios.liminal.prensentation.mangaReading.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaReadingTopBar(
    mangaName: String,
    chapterName: String,
    onNavigateUp: () -> Unit,
    onClickReadModeIcon: () -> Unit,
    onClickChapterListIcon: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = mangaName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = chapterName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }, navigationIcon = {
            IconButton(onClick = { onNavigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { onClickReadModeIcon() }) {
                Icon(Icons.Filled.RemoveRedEye, null) // TODO: Iconu değştir
            }
            IconButton(onClick = { onClickChapterListIcon() }) {
                Icon(Icons.AutoMirrored.Filled.List, null)
            }
        }


    )
}