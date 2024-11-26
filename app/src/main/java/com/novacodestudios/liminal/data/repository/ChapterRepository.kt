package com.novacodestudios.liminal.data.repository

import com.novacodestudios.liminal.data.locale.ChapterDao
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import com.novacodestudios.liminal.util.executeWithResourceFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChapterRepository @Inject constructor(
    private val dao: ChapterDao
) {
    // TODO: Dao fonksiyonlarını resource dan kurtar

    fun getChapters(seriesId: String): Flow<Resource<List<ChapterEntity>>> =
        executeWithResourceFlow {
            dao.getChapters(seriesId)
        }
    fun getChaptersNew(seriesId: String): Flow<List<ChapterEntity>> = dao.getChapters(seriesId)


    fun getChapter(chapterId: String): Flow<Resource<ChapterEntity>> = executeWithResourceFlow {
        dao.getChapter(chapterId)
    }
    fun getChapterNew(chapterId: String): Flow<ChapterEntity> =
        dao.getChapter(chapterId)


    suspend fun upsertChapter(chapter: ChapterEntity) = executeWithResource {
        dao.upsert(chapter)
    }

    suspend fun insertAllChapters(chapters: List<ChapterEntity>) = executeWithResource {
        dao.insertAllChapters(chapters)
    }

    suspend fun setIsReadByChapterId(chapterId: String, isRead: Boolean) = executeWithResource {
        dao.setIsReadByChapterId(chapterId, isRead)
    }

    suspend fun deleteChapter(chapter: ChapterEntity) = executeWithResource {
        dao.delete(chapter)
    }


    fun getChapterByIndex(index: Int, seriesId: String): Flow<Resource<ChapterEntity>> =
        executeWithResourceFlow {
            dao.getChapterByIndex(index, seriesId)
        }


    suspend fun updateChapterDownloadPath(chapterId: String,path:String?)=
        withContext(Dispatchers.IO){
            dao.updateChapterDownloadPath(chapterId, path)
        }

}

