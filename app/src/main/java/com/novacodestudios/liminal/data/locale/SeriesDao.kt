package com.novacodestudios.liminal.data.locale

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Upsert
    suspend fun upsert(series: SeriesEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(series: SeriesEntity)

    @Delete
    suspend fun deleteSeries(series: SeriesEntity)

    @Query("SELECT * FROM SeriesEntity WHERE id = :id")
    fun getSeriesById(id: String): Flow<SeriesEntity>

    @Query("SELECT * FROM SeriesEntity")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM SeriesEntity WHERE id=(SELECT seriesId FROM ChapterEntity WHERE id=:chapterId)")
    fun getSeriesEntityByChapterId(chapterId: String): Flow<SeriesEntity>
}