package com.novacodestudios.liminal.data.remote

import android.content.Context
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class SadScansScrapper @Inject constructor(private val context: Context) : MangaScraper {
    override val baseUrl: String
        get() = "https://sadscans.com/"


    override suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto {
        val document:Document = Jsoup.connect(detailPageUrl).get()

        return MangaDetailDto(
            name = document.selectFirst("div.title h2")?.text()
                ?: "",
            imageUrl = (baseUrl + document.selectFirst("div.series-image img")?.attr("src")),
            summary = document.selectFirst("div.summary p")?.text()
                ?: "",  // "summary" sınıfındaki div içindeki p etiketindeki metni al
            author = document.select("div.author span").getOrNull(1)?.text()
                ?: "",
            chapters = emptyList()
        )
    }

    override suspend fun getMangaChapterImages(chapterUrl: String): List<String> {
        val document: Document = Jsoup.connect(chapterUrl).get()

        return document.select("img").mapNotNull { element ->
            val src = element.attr("src")
            if (src.startsWith("htt")) src else null
        }
    }

    override suspend fun getMangaList(pageNumber: Int): List<MangaPreviewDto> {
        val url = baseUrl + "series"
        val document = Jsoup.connect(url).get()

        return document.select("div.series-list").map { element ->
            MangaPreviewDto(
                name = element.select("h2").text(),
                imageUrl = baseUrl + element.select("img").attr("data-src"),
                detailPageUrl = baseUrl + element.select("a.button").attr("href"),
                source = "sadscans"
            )
        }
    }

    override suspend fun getMangaChapterList(detailPageUrl: String): List<ChapterDto> {
        val document: Document = Jsoup.connect(detailPageUrl).get()

        return document.select("div.chap div.link").map { element ->
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