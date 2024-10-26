package com.novacodestudios.liminal.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.novacodestudios.liminal.data.remote.MangaScraper
import com.novacodestudios.liminal.data.remote.SadScansScrapper
import com.novacodestudios.liminal.data.remote.TempestScrapper
import com.novacodestudios.liminal.data.remote.pagingSource.TempestPagingSource
import com.novacodestudios.liminal.domain.mapper.toChapter
import com.novacodestudios.liminal.domain.mapper.toMangaDetail
import com.novacodestudios.liminal.domain.mapper.toMangaPreviewList
import com.novacodestudios.liminal.domain.model.Chapter
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

    /*
    tempesette her sayfa başına 50 manga veriyor
     */
    fun getTempestMangas(): Flow<PagingData<MangaPreview>> =
        Pager(
            config = PagingConfig(pageSize = 50),
            pagingSourceFactory = {
                TempestPagingSource(
                    scraper = tempestScrapper,
                )
            }
            ).flow

    fun getSadScanMangas(): Flow<Resource<List<MangaPreview>>> = executeWithResource {
        sadScansScrapper.getMangaList().toMangaPreviewList()
    }

    // TODO: Design patern uygula
    fun getMangaDetail(detailPageUrl: String): Flow<Resource<MangaDetail>> = executeWithResource {
        when {
            detailPageUrl.contains("sadscans") ->
                getMangaDetailByScrapper(sadScansScrapper, detailPageUrl)

            detailPageUrl.contains("tempestscans") -> getMangaDetailByScrapper(
                tempestScrapper,
                detailPageUrl
            )


            else -> throw IllegalArgumentException("Unsupported website")
        }
    }


    fun getMangaChapterList(detailPageUrl: String): Flow<Resource<List<Chapter>>> =
        executeWithResource {
            when {
                detailPageUrl.contains("sadscans") ->
                    sadScansScrapper.getMangaChapterList(detailPageUrl).map { it.toChapter() }

                detailPageUrl.contains("tempestscans") -> tempestScrapper.getMangaChapterList(
                    detailPageUrl
                ).map { it.toChapter() }


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
        executeWithResource(errorLog = {
            Log.e(
                TAG,
                "getMangaImageUrls: kapak resimleri yklenirken hata oluştu $it "
            )
        }) {
            when {
                chapterUrl.contains("sadscans") -> sadScansScrapper.getMangaChapterImages(chapterUrl)
                chapterUrl.contains("tempestscans") -> tempestScrapper.getMangaChapterImages(
                    chapterUrl
                )

                else -> throw IllegalArgumentException("Unsupported website")
            }
        }

    companion object {
        private const val TAG = "MangaRepository"
    }

}