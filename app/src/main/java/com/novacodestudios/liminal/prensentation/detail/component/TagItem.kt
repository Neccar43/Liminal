package com.novacodestudios.liminal.prensentation.detail.component

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.novacodestudios.liminal.domain.model.Tag

@Composable
fun TagChip(modifier: Modifier = Modifier, tag: Tag, onTagClick: (Tag) -> Unit) {
    AssistChip(
        onClick = { onTagClick(tag) },
        label = { Text(tag.name) },
        modifier = modifier
    )
}