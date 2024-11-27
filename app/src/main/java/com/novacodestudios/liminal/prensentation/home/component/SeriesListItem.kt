package com.novacodestudios.liminal.prensentation.home.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.novacodestudios.liminal.domain.model.SeriesPreview

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SeriesListItem(
    series: SeriesPreview,
    onClick: (SeriesPreview) -> Unit,
    itemHeight: Dp = 180.dp,
    itemWidth: Dp = 120.dp
) {
    Column(
        modifier = Modifier
            .clickable(onClick = { onClick(series) })
            .padding(8.dp)
            .width(itemWidth)
    ) {

        AsyncImage(
            model = series.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .height(itemHeight)
                .clip(shape = RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop
        )
        Text(
            text = series.name,
            modifier = Modifier
                .padding(8.dp),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

    }
}