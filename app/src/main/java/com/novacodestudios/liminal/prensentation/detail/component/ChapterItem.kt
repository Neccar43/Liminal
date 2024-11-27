package com.novacodestudios.liminal.prensentation.detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novacodestudios.liminal.domain.model.Chapter

@Composable
fun ChapterItem(
    modifier: Modifier = Modifier,
    chapter: Chapter,
    onChapterClick: (Chapter) -> Unit,
    onDownload: (Chapter) -> Unit,
    onDelete: (Chapter) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onChapterClick(chapter) },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chapter.releaseDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (chapter.filePath == null) {
            IconButton(
                onClick = { onDownload(chapter) },
            ) {

                Icon(
                    imageVector = Icons.Outlined.DownloadForOffline,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            IconButton(
                onClick = { onDelete(chapter) },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }


    }
}