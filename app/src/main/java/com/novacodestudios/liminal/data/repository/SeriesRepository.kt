package com.novacodestudios.liminal.data.repository

import com.novacodestudios.liminal.data.locale.SeriesDao
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import com.novacodestudios.liminal.util.executeWithResourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SeriesRepository @Inject constructor(
    private val dao: SeriesDao
) {
    fun getSeriesById(id: String): Flow<Resource<SeriesEntity>> = executeWithResourceFlow {
        dao.getSeriesById(id)
    }

    fun getAllSeries(): Flow<Resource<List<SeriesEntity>>> = executeWithResourceFlow {
        dao.getAllSeries()
    }

    suspend fun upsert(series: SeriesEntity): Flow<Resource<Unit>> = executeWithResource {
        dao.upsert(series)
    }

    suspend fun insertSeries(series: SeriesEntity): Flow<Resource<Unit>> = executeWithResource() {
        dao.insert(series)
    }

    suspend fun deleteSeries(series: SeriesEntity): Flow<Resource<Unit>> = executeWithResource {
        dao.deleteSeries(series)
    }

    fun getSeriesByChapterId(chapterId: String): Flow<Resource<SeriesEntity>> =
        executeWithResourceFlow {
            dao.getSeriesEntityByChapterId(chapterId)
        }

    companion object {
        private const val TAG = "SeriesRepository"
    }


}