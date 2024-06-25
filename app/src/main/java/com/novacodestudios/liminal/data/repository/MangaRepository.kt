package com.novacodestudios.liminal.data.repository

import android.util.Log
import com.novacodestudios.liminal.data.remote.MangaScraper
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.domain.mapper.toChapter
import com.novacodestudios.liminal.domain.mapper.toMangaDetail
import com.novacodestudios.liminal.domain.mapper.toMangaPreviewList
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.MangaDetail
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val tempestScrapper: TempestScrapper,
    private val sadScansScrapper: SadScansScrapper
) {


    fun getMangas(pageNumber: Int = 1): Flow<Resource<List<MangaPreview>>> =
        executeWithResource(errorLog = { Log.e(TAG, "getMangas: Error: $it")}) {
            coroutineScope {
                val sadScansDeferred = async { sadScansScrapper.getMangaList() }
                val tempestDeferred = async { tempestScrapper.getMangaList(pageNumber) }

                val sadScansMangas = sadScansDeferred.await()
                val tempestMangas = tempestDeferred.await()

                val sum = sadScansMangas + tempestMangas
                sum.toMangaPreviewList()
            }
        }




    // TODO: Design patern uygula
    fun getMangaDetail(detailPageUrl: String): Flow<Resource<MangaDetail>> = executeWithResource {
        when {
            detailPageUrl.contains("sadscans") ->
                getMangaDetailByScrapper(sadScansScrapper, detailPageUrl)

            detailPageUrl.contains("tempestfansub") -> getMangaDetailByScrapper(
                tempestScrapper,
                detailPageUrl
            )


            else -> throw IllegalArgumentException("Unsupported website")
        }
    }


    fun getMangaChapterList(detailPageUrl: String): Flow<Resource<List<Chapter>>> = executeWithResource {
        when {
            detailPageUrl.contains("sadscans") ->
                sadScansScrapper.getMangaChapterList(detailPageUrl).map { it.toChapter() }

            detailPageUrl.contains("tempestfansub") -> tempestScrapper.getMangaChapterList(detailPageUrl).map { it.toChapter() }


            else -> throw IllegalArgumentException("Unsupported website")
        }
    }

    private suspend fun getMangaDetailByScrapper(
        scraper: MangaScraper,
        detailPageUrl: String
    ): MangaDetail {
        return scraper.getMangaDetail(detailPageUrl).toMangaDetail()


    }

    // TODO: Design patern uygula
    fun getMangaImageUrls(chapterUrl: String): Flow<Resource<List<String>>> =
        executeWithResource(errorLog = { Log.e(TAG, "getMangaImageUrls: kapak resimleri yklenirken hata oluştu $it ")}) {
        when {
            chapterUrl.contains("sadscans") -> sadScansScrapper.getMangaChapterImages(chapterUrl)
            chapterUrl.contains("tempestfansub") -> tempestScrapper.getMangaChapterImages(chapterUrl)
            else -> throw IllegalArgumentException("Unsupported website")
        }
    }

    companion object{
        private const val TAG = "MangaRepository"
    }

}