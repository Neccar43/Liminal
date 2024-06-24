package com.novacodestudios.liminal.data.repository

import android.util.Log
import com.novacodestudios.liminal.data.cache.novelDetailCache
import com.novacodestudios.liminal.data.cache.novelPreviewCache
import com.novacodestudios.liminal.data.remote.TurkceLightNovelScrapper
import com.novacodestudios.liminal.domain.mapper.toNovelDetail
import com.novacodestudios.liminal.domain.mapper.toNovelPreviewList
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
            failLog = { Log.e(TAG, "getNovelList: error: $it")}
        ) {
        val cache = novelPreviewCache
        val cachedPreviews = cache.get(Unit)
        if (cachedPreviews.isNullOrEmpty()) {
            cache.invalidateAll()
            val scrapedPreviews = turkceLightNovelScrapper.getNovelList().toNovelPreviewList()
            cache.put(Unit, scrapedPreviews)
            scrapedPreviews
        } else {
            cachedPreviews
        }
    }

    fun getNovelChapterContent(chapterUrl: String): Flow<Resource<List<String>>> =
        executeWithResource {
            turkceLightNovelScrapper.getNovelChapterContent(chapterUrl)
        }

    fun getNovelDetail(detailPageUrl: String): Flow<Resource<NovelDetail>> = executeWithResource {
        val cache = novelDetailCache
        val cachedDetail = cache.get(detailPageUrl)
        Log.d(TAG, "getNovelDetail: çalıştı")
        if (cachedDetail == null) {
            cache.invalidateAll()
            val scrapedDetail =
                turkceLightNovelScrapper.getNovelDetail(detailPageUrl).toNovelDetail()
            cache.put(detailPageUrl, scrapedDetail)
            Log.d(TAG, "getNovelDetail: veri remote dan getirildi")
            scrapedDetail
        } else {
            Log.d(TAG, "getNovelDetail: veri cache dan getirildi")
            cachedDetail
        }
    }

    companion object {
        private const val TAG = "NovelRepository"
    }

}