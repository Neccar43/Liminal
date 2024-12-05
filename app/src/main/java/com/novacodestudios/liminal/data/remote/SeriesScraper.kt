package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.SeriesDto
import com.novacodestudios.liminal.data.remote.dto.SeriesSummaryDto
import com.novacodestudios.liminal.domain.model.Content
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.util.Result

interface SeriesScraper {
    val source: Source
    val baseUrl: String
        get() = source.url

    suspend fun getSeriesList(pageNumber: Int = 1): Result<List<SeriesSummaryDto>, DataError.Network>
    suspend fun getSeriesChapterList(detailPageUrl: String): Result<List<ChapterDto>, DataError.Network>
    suspend fun getSeriesDetail(detailPageUrl: String): Result<SeriesDto, DataError.Network>
    suspend fun getChapterContent(chapterUrl: String): Result<List<Content>, DataError.Network>
}