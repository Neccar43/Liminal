package com.novacodestudios.liminal.prensentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RetryStateMessage(
    modifier: Modifier = Modifier,
    errorMessage: String,
    icon: ImageVector = Icons.Default.Error,
    iconTint: Color = MaterialTheme.colorScheme.error,
    messageStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    iconSize: Dp = 64.dp,
    spacing: Dp = 16.dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(spacing)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(spacing))
            Text(
                text = errorMessage,
                style = messageStyle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing))
            Button(onClick = onClick) {
                Text(text = "Yeniden Dene")
            }
        }
    }
}