package com.novacodestudios.liminal.prensentation.mangaReading.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novacodestudios.liminal.prensentation.mangaReading.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadModeBottomSheet(
    modifier: Modifier,
    onDismiss: () -> Unit,
    onItemClick: (ReadingMode) -> Unit,
) {
    ModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
        ReadingMode.entries.forEach { mode ->
            SheetItem(title = mode.modeName, onClick = { onItemClick(mode); onDismiss() })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SheetItem(modifier: Modifier = Modifier, title: String, onClick: () -> Unit) {
    Text(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        text = title,
        style = MaterialTheme.typography.bodyLarge,
    )
}