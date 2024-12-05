package com.novacodestudios.liminal.prensentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager

// TODO: Animason Dışarıdan verilebilir
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiminalSearchBarWithAnimation(
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    // onSearch: (String) -> Unit, //Aramayı her harf için istediğimizden buna gerek yok
    placeholderText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(active) {
        if (active) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    AnimatedVisibility(
        visible = active,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        SearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onQueryChange,
                    expanded = active,
                    onExpandedChange = onActiveChange,
                    placeholder = { Text(placeholderText) },
                    leadingIcon = {
                        IconButton(onClick = {
                            onActiveChange(false)
                            onQueryChange("")
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                onQueryChange("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null
                                )
                            }
                        }
                    } else {
                        null
                    },
                )
            },
            expanded = active,
            onExpandedChange = onActiveChange,
            content = content,
        )
    }
}
