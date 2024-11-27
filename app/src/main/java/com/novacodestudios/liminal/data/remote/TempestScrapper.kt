package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import com.novacodestudios.liminal.data.remote.dto.Tag
import com.novacodestudios.liminal.domain.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

class TempestScrapper : MangaScraper {
    override val source: Source
        get() = Source.TEMPEST

    override suspend fun getMangaList(pageNumber: Int): List<MangaPreviewDto> =
        withContext(Dispatchers.IO) {
            val url = baseUrl + "manga/?page=$pageNumber"
            val document: Document = Jsoup.connect(url).get()

            document.select("div.bsx").map { element ->
                MangaPreviewDto(
                    name = element.selectFirst("a")?.attr("title") ?: "",
                    imageUrl = element.selectFirst("img")?.attr("src") ?: "",
                    detailPageUrl = element.selectFirst("a")?.attr("href") ?: "",
                    source = "tempestfansub"
                )
            }
        }

    override suspend fun getMangaChapterList(detailPageUrl: String): List<ChapterDto> =
        withContext(Dispatchers.IO) {
            val document: Document = Jsoup.connect(detailPageUrl).get()

            document.select("div#chapterlist.eplister ul li").map { element ->
                ChapterDto(
                    title = element.selectFirst("a")?.text() ?: "",
                    url = element.selectFirst("a")?.attr("href") ?: "",
                    releaseDate = "-"
                )
            }
        }

    override suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto = withContext(Dispatchers.IO) {
        val document: Document = Jsoup.connect(detailPageUrl).get()

        MangaDetailDto(
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
            chapters = emptyList()
        )
    }


    override suspend fun getMangaChapterImages(chapterUrl: String): List<String> =
        withContext(Dispatchers.IO) {
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
                    return@withContext imagesList.split(",")
                        .map { it.trim().replace("\"", "").replace("\\/", "/") }
                }
            }

            // Eğer URL bulunmazsa boş liste döndür
            return@withContext emptyList<String>()
        }

    companion object {
        private const val TAG = "TempestScrapper"


        private val client by lazy {
            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Bağlantı için timeout (default: 10 saniye)
                .readTimeout(60, TimeUnit.SECONDS)     // Okuma için timeout (default: 10 saniye)
                .writeTimeout(60, TimeUnit.SECONDS).build()
        }
    }
}




