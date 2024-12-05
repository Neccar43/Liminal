package com.novacodestudios.liminal.data.repository

import android.database.sqlite.SQLiteException
import com.novacodestudios.liminal.data.locale.SeriesDao
import com.novacodestudios.liminal.data.mapper.toChapter
import com.novacodestudios.liminal.data.mapper.toModel
import com.novacodestudios.liminal.data.mapper.toSeriesDetail
import com.novacodestudios.liminal.data.mapper.toSeriesEntity
import com.novacodestudios.liminal.data.remote.SeriesScraper
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.Content
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.model.SeriesSummary
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.util.EmptyResult
import com.novacodestudios.liminal.domain.util.Result
import com.novacodestudios.liminal.domain.util.map
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SeriesRepository @Inject constructor(
    private val scrapers: List<SeriesScraper>,
    private val dao: SeriesDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private fun getScraperForUrl(url: String): SeriesScraper {
        return scrapers.find { url.contains(it.source.url) }
            ?: throw IllegalArgumentException("Invalid source") // TODO: exceptionu kaydet
    }

    suspend fun getSeriesList(
        source: Source,
        page: Int = 1
    ): Result<List<SeriesSummary>, DataError.Network> = withContext(dispatcher) {
        val scraper = getScraperForUrl(source.url)
        scraper.getSeriesList(page).map { it.map { x -> x.toModel() } }
    }

    suspend fun getSeriesDetail(url: String): Result<Series, DataError.Network> =
        withContext(dispatcher) {
            val scraper = getScraperForUrl(url)
            scraper.getSeriesDetail(url).map { it.toSeriesDetail() }
        }

    suspend fun getSeriesChapterList(url: String): Result<List<Chapter>, DataError.Network> =
        withContext(dispatcher) {
            val scraper = getScraperForUrl(url)
            scraper.getSeriesChapterList(url)
                .map { it.map { chapterDto -> chapterDto.toChapter() } }
        }

    suspend fun getChapterContent(chapterUrl: String): Result<List<Content>, DataError.Network> =
        withContext(dispatcher) {
            val scraper = getScraperForUrl(chapterUrl)
            scraper.getChapterContent(chapterUrl)
        }


    suspend fun getSeriesById(id: String): Series {
        return dao.getSeriesById(id)?.toSeriesDetail()
            ?: throw IllegalArgumentException("Seri bulunamadı: $id")
    }

    fun getAllSeries(): Flow<List<Series>> {
        return dao.getAllSeries().map { it.map { entity -> entity.toSeriesDetail() } }
    }

    suspend fun upsert(series: Series, currentChapter: Chapter): EmptyResult<DataError.Local> {
        return try {
            dao.upsert(series.toSeriesEntity(currentChapter))
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    suspend fun deleteSeriesById(id: String) {
        dao.deleteSeriesById(id)
    }

    suspend fun getSeriesByChapterId(chapterId: String): Series {
        return dao.getSeriesEntityByChapterId(chapterId)?.toSeriesDetail()
            ?: throw IllegalArgumentException("Seri bulunamadı: $chapterId")
    }

}