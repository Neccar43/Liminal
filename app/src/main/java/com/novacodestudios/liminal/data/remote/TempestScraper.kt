package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.SeriesDto
import com.novacodestudios.liminal.data.remote.dto.SeriesSummaryDto
import com.novacodestudios.liminal.data.util.safeCall
import com.novacodestudios.liminal.domain.model.Content
import com.novacodestudios.liminal.domain.model.DataError
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.Source
import com.novacodestudios.liminal.domain.model.Tag
import com.novacodestudios.liminal.domain.util.Result
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class TempestScraper(
) : SeriesScraper {
    override val source: Source
        get() = Source.TEMPEST

    override suspend fun getSeriesList(pageNumber: Int): Result<List<SeriesSummaryDto>, DataError.Network> =
        safeCall {
            val url = baseUrl + "manga/?page=$pageNumber"
            val document: Document = Jsoup.connect(url).get()

            document.select("div.bsx").map { element ->
                SeriesSummaryDto(
                    name = element.selectFirst("a")?.attr("title") ?: "",
                    imageUrl = element.selectFirst("img")?.attr("src") ?: "",
                    detailPageUrl = element.selectFirst("a")?.attr("href") ?: "",
                    source = source,
                    seriesType = SeriesType.MANGA
                )
            }
        }


    override suspend fun getSeriesChapterList(detailPageUrl: String): Result<List<ChapterDto>, DataError.Network> =
        safeCall {
            val document: Document = Jsoup.connect(detailPageUrl).get()

            document.select("div#chapterlist.eplister ul li").map { element ->
                ChapterDto(
                    title = element.selectFirst("a")?.text() ?: "",
                    url = element.selectFirst("a")?.attr("href") ?: "",
                    releaseDate = "-"
                )
            }
        }


    override suspend fun getSeriesDetail(detailPageUrl: String): Result<SeriesDto, DataError.Network> =
        safeCall {
            val document: Document = Jsoup.connect(detailPageUrl).get()

            SeriesDto(
                name = document.selectFirst("h1.entry-title")?.text() ?: "",
                imageUrl = document.selectFirst("div.thumb img")?.attr("src") ?: "",
                summary = document.selectFirst("p")?.text() ?: "",
                author = document.select("div.imptdt i").getOrNull(2)?.text() ?: "",
                source = source,
                status = document.select("div.imptdt i").getOrNull(0)?.text() ?: "",
                tags = document.select("span.mgen a").map { element ->
                    val url = element.attr("href")
                    val name = element.text()
                    Tag(url = url, name = name)
                },
                chapters = emptyList(),
                seriesType = SeriesType.MANGA,
                detailPageUrl = detailPageUrl
            )
        }


    override suspend fun getChapterContent(chapterUrl: String): Result<List<Content>, DataError.Network> =
        safeCall {
            // HTML sayfasını al
            val doc = Jsoup.connect(chapterUrl).get()

            // "ts_reader.run" içeren <script> etiketini bul
            val scriptElement =
                doc.select("script").firstOrNull { it.data().contains("ts_reader.run") }

            if (scriptElement != null) {
                val scriptData = scriptElement.data()

                // "images" listesini Regex ile bul
                val regex = """"images":\s*\[(.*?)\]""".toRegex()
                val matchResult = regex.find(scriptData)
                val imagesList = matchResult?.groups?.get(1)?.value

                if (imagesList != null) {
                    // URL'leri parçala ve escape karakterlerini düzelt
                    return@safeCall imagesList.split(",")
                        .map { it.trim().replace("\"", "").replace("\\/", "/") }
                        .map { Content.Image(url = it) }
                }
            }

            // Eğer URL bulunmazsa boş liste döndür
            return@safeCall emptyList<Content>()
        }


    companion object {
        private const val TAG = "TempestScrapper"
    }
}




