package com.novacodestudios.liminal.data.repository

import android.util.Log
import com.novacodestudios.liminal.data.cache.mangaDetailCache
import com.novacodestudios.liminal.data.cache.mangaPreviewCache
import com.novacodestudios.liminal.data.cache.novelDetailCache
import com.novacodestudios.liminal.data.cache.novelPreviewCache
import com.novacodestudios.liminal.data.remote.MangaScraper
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.domain.mapper.toMangaDetail
import com.novacodestudios.liminal.domain.mapper.toMangaPreviewList
import com.novacodestudios.liminal.domain.mapper.toNovelPreviewList
import com.novacodestudios.liminal.domain.model.MangaDetail
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.executeWithResource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MangaRepository @Inject constructor(
    private val tempestScrapper: TempestScrapper,
    private val sadScansScrapper: SadScansScrapper
) {


    fun getMangas(pageNumber: Int = 1): Flow<Resource<List<MangaPreview>>> =
        executeWithResource {
            val cache= mangaPreviewCache
            val cachedPreviews=cache.get(Unit)
            if (cachedPreviews.isNullOrEmpty()){
                cache.invalidateAll()
                val sadScansMangas = sadScansScrapper.getMangaList()
                val tempestMangas = tempestScrapper.getMangaList(pageNumber)
                val sum = sadScansMangas + tempestMangas
                val scrapedPreviews= sum.toMangaPreviewList()
                cache.put(Unit,scrapedPreviews)
                scrapedPreviews
            }else{
                cachedPreviews
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

    private suspend fun getMangaDetailByScrapper(
        scraper: MangaScraper,
        detailPageUrl: String
    ): MangaDetail {
        val cache = mangaDetailCache
        val cachedDetail = cache.get(detailPageUrl)
        return if (cachedDetail == null) {
            cache.invalidateAll()
            val scrapedDetail = scraper.getMangaDetail(detailPageUrl).toMangaDetail()
            cache.put(detailPageUrl, scrapedDetail)
            Log.d(TAG, "getMangaDetailByScrapper: remote dan yüklendi")
            scrapedDetail
        } else {
            Log.d(TAG, "getMangaDetailByScrapper: cache den yüklendi")
            cachedDetail
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

    companion object{
        private const val TAG = "MangaRepository"
    }

}