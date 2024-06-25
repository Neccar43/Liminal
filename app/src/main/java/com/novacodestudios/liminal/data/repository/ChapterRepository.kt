package com.novacodestudios.liminal.data.repository

import com.novacodestudios.liminal.data.locale.ChapterDao
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import com.novacodestudios.liminal.util.executeWithResourceFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChapterRepository @Inject constructor(
     private val chapterDao: ChapterDao
) {

    fun getChapters(seriesId: String): Flow<Resource<List<ChapterEntity>>> = executeWithResourceFlow {
        chapterDao.getChapters(seriesId)
    }

    fun getChapter(chapterId: String): Flow<Resource<ChapterEntity>> = executeWithResourceFlow {
        chapterDao.getChapter(chapterId)
    }

    suspend fun upsertChapter(chapter: ChapterEntity) = executeWithResource{
        chapterDao.upsert(chapter)
    }

    suspend fun insertAllChapters(chapters: List<ChapterEntity>) = executeWithResource{
        chapterDao.insertAllChapters(chapters)
    }

    suspend fun deleteChapter(chapter: ChapterEntity) = executeWithResource{
        chapterDao.delete(chapter)
    }

    suspend fun resetIsRead(seriesId: String) = executeWithResource{
        chapterDao.resetIsRead(seriesId)
    }

    fun getChapterByIndex(index: Int, seriesId: String): Flow<Resource<ChapterEntity>> = executeWithResourceFlow{
        chapterDao.getChapterByIndex(index, seriesId)
    }

    suspend fun markChapterAsReadAndUpdateCurrentChapter(chapterId:String): Flow<Resource<Unit>> = executeWithResource {
        chapterDao.markChapterAsReadAndUpdateCurrentChapter(chapterId)
    }

}

