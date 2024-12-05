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
import com.novacodestudios.liminal.domain.model.SeriesSummary
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.prensentation.util.getDisplayName

@Composable
fun SeriesGridList(
    modifier: Modifier = Modifier,
    source: Source,
    seriesList: List<SeriesSummary>,
    onClick: (SeriesSummary) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = "${source.getDisplayName()} Kaynağından",
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