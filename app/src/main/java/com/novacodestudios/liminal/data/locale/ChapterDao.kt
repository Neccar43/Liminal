package com.novacodestudios.liminal.data.locale

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Dao
interface ChapterDao {

    @Upsert
    suspend fun upsert(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllChapters(chapters: List<ChapterEntity>)

    @Delete
    suspend fun delete(chapter: ChapterEntity)

    @Query("SELECT * FROM chapterentity WHERE seriesId = :seriesId")
     fun getChapters(seriesId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapterentity WHERE id = :chapterId")
     fun getChapter(chapterId: String): Flow<ChapterEntity>

    @Query("UPDATE chapterentity SET isRead = 0 WHERE seriesId = :seriesId")
    suspend fun resetIsRead(seriesId: String)

    @Query("SELECT * FROM chapterentity WHERE pageIndex = :index AND seriesId = :seriesId")
    fun getChapterByIndex(index: Int, seriesId: String): Flow<ChapterEntity>

    @Transaction
    suspend fun markChapterAsReadAndUpdateCurrentChapter(chapterId: String) {
        getSeriesIdByChapterId(chapterId).collectLatest {seriesId->
            updateChapterReadStatus(chapterId)
            updateSeriesCurrentChapter(chapterId, seriesId)
        }

    }

    @Query("SELECT seriesId FROM ChapterEntity WHERE id = :chapterId")
     fun getSeriesIdByChapterId(chapterId: String): Flow<String>

    @Query("UPDATE ChapterEntity SET isRead = 1 WHERE id = :chapterId")
    suspend fun updateChapterReadStatus(chapterId: String)

    @Query("UPDATE SeriesEntity SET currentChapterId = :chapterId WHERE id = :seriesId")
    suspend fun updateSeriesCurrentChapter(chapterId: String, seriesId: String)

}