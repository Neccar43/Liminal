package com.novacodestudios.liminal.data.repository

import android.util.Log
import com.novacodestudios.liminal.data.remote.TurkceLightNovelScrapper
import com.novacodestudios.liminal.domain.mapper.toChapter
import com.novacodestudios.liminal.domain.mapper.toNovelDetail
import com.novacodestudios.liminal.domain.mapper.toNovelPreviewList
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.NovelDetail
import com.novacodestudios.liminal.domain.model.NovelPreview
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NovelRepository @Inject constructor(
    private val turkceLightNovelScrapper: TurkceLightNovelScrapper
) {
    fun getNovelList(): Flow<Resource<List<NovelPreview>>> =
        executeWithResource(
            errorLog = { Log.e(TAG, "getNovelList: error: $it") }
        ) {
            turkceLightNovelScrapper.getNovelList().toNovelPreviewList()

        }

    fun getNovelChapterContent(chapterUrl: String): Flow<Resource<List<String>>> =
        executeWithResource {
            turkceLightNovelScrapper.getNovelChapterContent(chapterUrl)
        }


    fun getNovelDetail(detailPageUrl: String): Flow<Resource<NovelDetail>> =
        executeWithResource(
            errorLog = { Log.e(TAG, "getNovelDetail: $it") }
        ) {
            val scrapedDetail =
                turkceLightNovelScrapper.getNovelDetail(detailPageUrl).toNovelDetail()
            scrapedDetail
        }

    fun getNovelChapters(detailPageUrl: String): Flow<Resource<List<Chapter>>> =
        executeWithResource {
            turkceLightNovelScrapper
                .getNovelChapterUrls(detailPageUrl = detailPageUrl)
                .map { it.toChapter() }
        }

    companion object {
        private const val TAG = "NovelRepository"
    }

}