package com.novacodestudios.liminal.prensentation.novelReading

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope

@Composable
fun NovelReadingScreen(
    viewModel: NovelReadingViewModel= hiltViewModel(),
    onNavigateUp:()->Unit,

) {
    NovelReadingContent(
        state = viewModel.state,
        onNavigateUp = onNavigateUp,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun NovelReadingContent(
    state:NovelReadingState,
    onEvent:(NovelEvent)->Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {}
    ) {innerPadding->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            state.chapterContent.forEach { paragraph ->
                if (paragraph.isNotBlank()) {
                    Text(
                        text = paragraph.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom=8.dp)
                    )
                }
            }
        }
    }

}