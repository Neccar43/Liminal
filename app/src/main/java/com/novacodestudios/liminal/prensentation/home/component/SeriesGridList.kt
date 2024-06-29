package com.novacodestudios.liminal.prensentation.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.util.capitalizeFirstLetter

@Composable
fun SeriesGridList(
    modifier: Modifier = Modifier,
    source: String,
    seriesList: List<SeriesPreview>,
    onClick: (SeriesPreview) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = "${source.capitalizeFirstLetter()} Kaynağından",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(seriesList.chunked(2)) { chunk ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    chunk.forEach { series ->
                        SeriesListItem(
                            series = series,
                            onClick = { onClick(series) }
                        )
                    }
                }
            }
        }
    }
}