package com.novacodestudios.liminal.data.repository

import com.novacodestudios.liminal.data.remote.TurkceLightNovelScrapper
import com.novacodestudios.liminal.domain.mapper.toNovelDetail
import com.novacodestudios.liminal.domain.mapper.toNovelPreviewList
import com.novacodestudios.liminal.domain.model.NovelDetail
import com.novacodestudios.liminal.domain.model.NovelPreview
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NovelRepository @Inject constructor(
    private val turkceLightNovelScrapper: TurkceLightNovelScrapper
) {
    val novelListCache = Cache.Builder<Unit, NovelDetail>()
        .build()

    fun getNovelList(): Flow<Resource<List<NovelPreview>>> = executeWithResource {

        novelListCache.invalidateAll()

        turkceLightNovelScrapper.getNovelList().toNovelPreviewList()
    }

    fun getNovelChapterContent(chapterUrl: String): Flow<Resource<List<String>>> =
        executeWithResource {
            turkceLightNovelScrapper.getNovelChapterContent(chapterUrl)
        }

    fun getNovelDetail(detailPageUrl: String): Flow<Resource<NovelDetail>> = executeWithResource {
        turkceLightNovelScrapper.getNovelDetail(detailPageUrl).toNovelDetail()
    }


}