package com.novacodestudios.liminal.data.locale

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    @Upsert
    suspend fun upsert(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllChapters(chapters: List<ChapterEntity>)

    @Delete
    suspend fun delete(chapter: ChapterEntity)

    @Query("SELECT * FROM chapterentity WHERE id = :seriesId")
     fun getChapters(seriesId: Int): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapterentity WHERE id = :chapterId")
     fun getChapter(chapterId: Int): Flow<ChapterEntity>

    @Query("UPDATE chapterentity SET isRead = 0 WHERE seriesId = :seriesId")
    suspend fun resetIsRead(seriesId: String)

    @Query("SELECT * FROM chapterentity WHERE `index` = :index AND seriesId = :seriesId")
    fun getChapterByIndex(index: Int, seriesId: String): Flow<ChapterEntity>

}