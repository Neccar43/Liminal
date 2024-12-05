package com.novacodestudios.liminal.data.locale

import androidx.room.Dao
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

    @Query("DELETE FROM SeriesEntity WHERE id=:id")
    suspend fun deleteSeriesById(id: String)

    @Query("SELECT * FROM SeriesEntity WHERE id = :id")
    suspend fun getSeriesById(id: String): SeriesEntity?

    @Query("SELECT * FROM SeriesEntity")
    fun getAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM SeriesEntity WHERE id=(SELECT seriesId FROM ChapterEntity WHERE id=:chapterId)")
    suspend fun getSeriesEntityByChapterId(chapterId: String): SeriesEntity?
}