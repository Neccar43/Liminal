package com.novacodestudios.liminal.data.repository

import android.database.sqlite.SQLiteException
import android.util.Log
import com.novacodestudios.liminal.data.locale.ChapterDao
import com.novacodestudios.liminal.data.mapper.toChapter
import com.novacodestudios.liminal.data.mapper.toChapterEntity
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.util.EmptyResult
import com.novacodestudios.liminal.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChapterRepository @Inject constructor(
    private val dao: ChapterDao
) {
    fun getChaptersBySeriesId(seriesId: String): Flow<List<Chapter>> =
        dao.getChapters(seriesId).map { it.map { chapterEntity -> chapterEntity.toChapter() } }


    suspend fun getChapterById(id: String): Chapter =
        dao.getChapter(id)?.toChapter() ?: throw IllegalArgumentException("Chapter bulunamadı: $id")


    suspend fun upsertChapter(
        chapter: Chapter,
        seriesId: String,
        pageIndex: Int
    ): EmptyResult<DataError.Local> {
        return try {
            dao.upsert(chapter.toChapterEntity(seriesId, pageIndex))
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }


    suspend fun insertAllChapters(
        chapters: List<Chapter>,
        series: Series
    ): EmptyResult<DataError.Local> {
        return try {
            dao.insertAllChapters(chapters.map {
                it.toChapterEntity(
                    seriesId = series.id,
                    index = 0
                )
            })
            Result.Success(Unit)
        } catch (e: SQLiteException) {
            Log.e(TAG, "insertAllChapters: hata ", e)
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    suspend fun setIsReadByChapterId(chapterId: String, isRead: Boolean) =
        dao.setIsReadByChapterId(chapterId, isRead)

    suspend fun updateChapterDownloadPath(chapterId: String, path: String?) =
        dao.updateChapterDownloadPath(chapterId, path)


    companion object {
        private const val TAG = "ChapterRepository"
    }
}

