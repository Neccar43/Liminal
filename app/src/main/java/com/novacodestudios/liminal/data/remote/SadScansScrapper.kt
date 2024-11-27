package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import com.novacodestudios.liminal.domain.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SadScansScrapper : MangaScraper {
    override val source: Source
        get() = Source.SADSCANS

    override suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto =
        withContext(Dispatchers.IO) {
            val document: Document = Jsoup.connect(detailPageUrl).get()

             MangaDetailDto(
                 name = document.selectFirst("div.title h2")?.text()
                     ?: "",
                 imageUrl = (baseUrl + document.selectFirst("div.series-image img")?.attr("src")),
                 summary = document.selectFirst("div.summary p")?.text()
                     ?: "",  // "summary" sınıfındaki div içindeki p etiketindeki metni al
                 author = document.select("div.author span").getOrNull(1)?.text()
                     ?: "",
                 chapters = emptyList(),
                 status = document.select("div.status span").getOrNull(1)?.text()
                     ?: "",
                 tags = emptyList(), //sadscans de tag kısmı yok
                 source = source,
             )
        }

    override suspend fun getMangaChapterImages(chapterUrl: String): List<String> = withContext(Dispatchers.IO){
        val document: Document = Jsoup.connect(chapterUrl).get()

         document.select("img").mapNotNull { element ->
            val src = element.attr("src")
            if (src.startsWith("htt")) src else null
        }
    }

    override suspend fun getMangaList(pageNumber: Int): List<MangaPreviewDto> = withContext(Dispatchers.IO){
        val url = baseUrl + "series"
        val document = Jsoup.connect(url).get()

         document.select("div.series-list").map { element ->
            MangaPreviewDto(
                name = element.select("h2").text(),
                imageUrl = baseUrl + element.select("img").attr("data-src"),
                detailPageUrl = baseUrl + element.select("a.button").attr("href"),
                source = "sadscans"
            )
        }
    }

    override suspend fun getMangaChapterList(detailPageUrl: String): List<ChapterDto> = withContext(Dispatchers.IO){
        val document: Document = Jsoup.connect(detailPageUrl).get()

         document.select("div.chap div.link").map { element ->
            ChapterDto(
                title = element.selectFirst("a")?.text() ?: "",
                url = baseUrl + (element.selectFirst("a")?.attr("href") ?: ""),
                releaseDate = "-"
            )
        }
    }

    companion object {
        private const val TAG = "SadScansScrapper"
    }
}