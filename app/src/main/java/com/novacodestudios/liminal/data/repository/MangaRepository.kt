package com.novacodestudios.liminal.data.repository

import android.content.Context
import com.gargoylesoftware.htmlunit.Page
import com.novacodestudios.liminal.data.remote.MangaScraper
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import com.novacodestudios.liminal.data.remote.dto.RecentMangaChaptersDto
import com.novacodestudios.liminal.domain.mapper.toMangaDetail
import com.novacodestudios.liminal.domain.mapper.toMangaPreviewList
import com.novacodestudios.liminal.domain.model.MangaDetail
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val tempestScrapper: TempestScrapper,
    private val sadScansScrapper: SadScansScrapper
) {
    /*fun getRecentMangaChapters(): Flow<Resource<List<RecentMangaChaptersDto>>> = executeWithResource{

    }*/

    fun getMangas(pageNumber: Int = 1): Flow<Resource<List<MangaPreview>>> =
        executeWithResource {
            val sadScansMangas = sadScansScrapper.getMangaList()
            val tempestMangas = tempestScrapper.getMangaList(pageNumber)
            val sum = sadScansMangas + tempestMangas
            sum.toMangaPreviewList()
        }

    // TODO: Design patern uygula
    fun getMangaDetail(detailPageUrl: String): Flow<Resource<MangaDetail>> = executeWithResource {
        when {
            detailPageUrl.contains("sadscans") -> sadScansScrapper.getMangaDetail(detailPageUrl)
                .toMangaDetail()

            detailPageUrl.contains("tempestfansub") -> tempestScrapper.getMangaDetail(detailPageUrl)
                .toMangaDetail()

            else -> throw IllegalArgumentException("Unsupported website")
        }
    }

    // TODO: Design patern uygula
    fun getMangaImageUrls(chapterUrl: String): Flow<Resource<List<String>>> = executeWithResource {
        when {
            chapterUrl.contains("sadscans") -> sadScansScrapper.getMangaChapterImages(chapterUrl)
            chapterUrl.contains("tempestfansub") -> tempestScrapper.getMangaChapterImages(chapterUrl)
            else -> throw IllegalArgumentException("Unsupported website")
        }
    }

}