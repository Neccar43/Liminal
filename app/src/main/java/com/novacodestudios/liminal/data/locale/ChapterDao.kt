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

    @Query("SELECT * FROM chapterentity WHERE seriesId = :seriesId")
    fun getChapters(seriesId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapterentity WHERE id = :chapterId")
    fun getChapter(chapterId: String): Flow<ChapterEntity> // TODO: Flowdan çıkar

    @Query("UPDATE chapterentity SET isRead = :isRead WHERE id = :chapterId")
    suspend fun setIsReadByChapterId(chapterId: String, isRead: Boolean)

    @Query("SELECT * FROM chapterentity WHERE pageIndex = :index AND seriesId = :seriesId")
    fun getChapterByIndex(index: Int, seriesId: String): Flow<ChapterEntity>


    @Query("SELECT seriesId FROM ChapterEntity WHERE id = :chapterId")
    suspend fun getSeriesIdByChapterId(chapterId: String): String // Directly get the seriesId

    @Query("UPDATE ChapterEntity SET isRead = 1 WHERE id = :chapterId")
    suspend fun updateChapterReadStatus(chapterId: String)

    @Query("UPDATE ChapterEntity SET downloadChapterPath=:path WHERE id=:chapterId")
    suspend fun updateChapterDownloadPath(chapterId: String,path:String?)


}