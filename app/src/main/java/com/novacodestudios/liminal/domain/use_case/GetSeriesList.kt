package com.novacodestudios.liminal.domain.use_case

import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.model.SeriesSummary
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.util.Result
import com.novacodestudios.liminal.domain.util.retryWithPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Domain katmanındaki repository'lerin kullanılması gerekiyor
class GetSeriesList @Inject constructor(
    private val seriesRepository: SeriesRepository,
) {
    operator fun invoke(): Flow<Result<List<SeriesSummary>, DataError.Network>> = channelFlow {
        val scrapers = Source.entries.map {
            if (it == Source.TEMPEST) {
                (1..3).map { page ->
                    async {
                        retryWithPolicy {
                            seriesRepository.getSeriesList(
                                it,
                                page
                            )
                        }
                    }
                }
            } else {
                listOf(async { retryWithPolicy { seriesRepository.getSeriesList(it) } })

            }
        }.flatten()

        scrapers.forEach { scraper ->
            launch {
                send(scraper.await())
            }
        }
    }.flowOn(Dispatchers.IO)

}
