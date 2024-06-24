package com.novacodestudios.liminal.data.repository

import com.novacodestudios.liminal.data.locale.SeriesDao
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import com.novacodestudios.liminal.util.executeWithResourceFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SeriesRepository @Inject constructor(
    private val seriesDao: SeriesDao
){
    fun getSeriesById(id: String): Flow<Resource<SeriesEntity>> = executeWithResourceFlow {
        seriesDao.getSeriesById(id)
    }

    fun getAllSeries(): Flow<Resource<List<SeriesEntity>>> = executeWithResourceFlow {
        seriesDao.getAllSeries()
    }

    suspend fun upsertSeries(series: SeriesEntity): Flow<Resource<Unit>> = executeWithResource{
        seriesDao.upsert(series)
    }

    suspend fun insertSeries(series: SeriesEntity): Flow<Resource<Unit>> = executeWithResource(){
        seriesDao.insertSeries(series)
    }

    suspend fun deleteSeries(series: SeriesEntity): Flow<Resource<Unit>> = executeWithResource{
        seriesDao.deleteSeries(series)
    }

    companion object{
        private const val TAG = "SeriesRepository"
    }


}