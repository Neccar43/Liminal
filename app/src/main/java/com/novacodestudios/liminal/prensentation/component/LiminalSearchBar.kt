package com.novacodestudios.liminal.prensentation.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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