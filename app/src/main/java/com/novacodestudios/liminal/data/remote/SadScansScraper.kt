package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.SeriesDto
import com.novacodestudios.liminal.data.remote.dto.SeriesSummaryDto
import com.novacodestudios.liminal.data.util.safeCall
import com.novacodestudios.liminal.domain.model.Content
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.util.Result
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SadScansScraper : SeriesScraper {
    override val source: Source
        get() = Source.SADSCANS

    override suspend fun getSeriesDetail(detailPageUrl: String): Result<SeriesDto, DataError.Network> =
        safeCall {
            val document: Document = Jsoup.connect(detailPageUrl).get()

            SeriesDto(
                name = document.selectFirst("div.title h2")?.text()
                    ?: "",
                imageUrl = (baseUrl + document.selectFirst("div.series-image img")
                    ?.attr("src")),
                summary = document.selectFirst("div.summary p")?.text()
                    ?: "",  // "summary" sınıfındaki div içindeki p etiketindeki metni al
                author = document.select("div.author span").getOrNull(1)?.text()
                    ?: "",
                chapters = emptyList(),
                status = document.select("div.status span").getOrNull(1)?.text()
                    ?: "",
                tags = emptyList(), //sadscans de tag kısmı yok
                source = source,
                seriesType = SeriesType.MANGA,
                detailPageUrl = detailPageUrl
            )
        }


    override suspend fun getChapterContent(chapterUrl: String): Result<List<Content>, DataError.Network> =
        safeCall {
            val document: Document = Jsoup.connect(chapterUrl).get()

            document.select("img").mapNotNull { element ->
                val src = element.attr("src")
                if (src.startsWith("htt")) Content.Image(url = src) else null
            }
        }


    override suspend fun getSeriesList(pageNumber: Int): Result<List<SeriesSummaryDto>, DataError.Network> =
        safeCall {
            val url = baseUrl + "series"
            val document = Jsoup.connect(url).get()

            document.select("div.series-list").map { element ->
                SeriesSummaryDto(
                    name = element.select("h2").text(),
                    imageUrl = baseUrl + element.select("img").attr("data-src"),
                    detailPageUrl = baseUrl + element.select("a.button").attr("href"),
                    source = source,
                    seriesType = SeriesType.MANGA
                )
            }
        }


    override suspend fun getSeriesChapterList(detailPageUrl: String): Result<List<ChapterDto>, DataError.Network> =
        safeCall {
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