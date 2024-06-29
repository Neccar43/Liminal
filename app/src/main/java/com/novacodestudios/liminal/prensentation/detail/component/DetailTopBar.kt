package com.novacodestudios.liminal.prensentation.detail.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(scrollBehavior: TopAppBarScrollBehavior, onNavigateUp: () -> Unit) {
    TopAppBar(
        modifier = Modifier,
        title = { Text(text = "Detail") },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = { onNavigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        })
}