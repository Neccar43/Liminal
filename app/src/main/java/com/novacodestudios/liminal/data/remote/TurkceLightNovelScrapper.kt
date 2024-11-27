package com.novacodestudios.liminal.data.remote

import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.NovelDetailDto
import com.novacodestudios.liminal.data.remote.dto.NovelPreviewDto
import com.novacodestudios.liminal.data.remote.dto.Tag
import com.novacodestudios.liminal.domain.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class TurkceLightNovelScrapper {

    private val baseUrl = "https://turkcelightnovels.com"

    // TODO: Aşağıdaki geçiçi çözümü kaldır
    private val urlIdMap = mutableMapOf<String, String>()


    suspend fun getNovelList(): List<NovelPreviewDto> = withContext(Dispatchers.IO) {
        urlIdMap.clear()
        val document: Document = Jsoup.connect(baseUrl)
            //.timeout(60_000)  // Timeout ayarını belirt
            .get()

        document.select("div.page-item-detail").map { element ->
            val detailPageUrl = element.selectFirst("h3.h5 a")?.attr("href") ?: ""
            val seriesRemoteId = element.selectFirst("div.item-thumb")?.attr("data-post-id") ?: ""
            urlIdMap[detailPageUrl] = seriesRemoteId

            NovelPreviewDto(
                name = element.selectFirst("h3.h5")?.text() ?: "",
                imageUrl = element.selectFirst("img")?.attr("data-src") ?: "",
                detailPageUrl = detailPageUrl,
                source = "turkcelightnovels",
            )
        }
    }

    private fun getNovelRemoteId(detailPageUrl: String): String? = urlIdMap[detailPageUrl]


    suspend fun getNovelDetail(detailPageUrl: String): NovelDetailDto =
        withContext(Dispatchers.IO) {
            val document: Document = Jsoup.connect(detailPageUrl)
                .get()

            var status=document.select("div.summary-content").last()?.text() ?: ""
            if (status=="OnGoing") {
                status="Devam ediyor"
            }

            NovelDetailDto(
                name = document.selectFirst("div.post-title h1")?.text() ?: "",
                imageUrl = document.selectFirst("div.summary_image img")?.attr("data-src") ?: "",
                author = document.selectFirst("div.author-content a")?.text() ?: "",
                rate = document.selectFirst("span.score")?.text() ?: "",
                summary = document.selectFirst("div.summary__content")?.text() ?: "",
                chapters = emptyList(),
                tags = document.select("div.genres-content a").map { element ->
                    val url = element.attr("href")
                    val name = element.text()
                    Tag(url = url, name = name)
                },
                source = Source.TURKCE_LIGHT_NOVEL,
                status = status,
            )
        }

    suspend fun getNovelChapterContent(chapterUrl: String): List<String> =
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(chapterUrl)
                .build()

            val response = client.newCall(request).execute()
            val htmlContent = response.body?.string() ?: ""

            parseHtmlContent(htmlContent)
        }

    private fun parseHtmlContent(html: String): List<String> {
        val doc: Document = Jsoup.parse(html)
        val elements: List<Element> = doc.select("div.text-left p, div.text-left img")
        val resultList = mutableListOf<String>()

        for (element in elements) {
            if (element.tagName() == "p") {
                resultList.add(element.text().trim())
            } else if (element.tagName() == "img") {
                resultList.add(element.attr("src"))
            }
        }
        return resultList
    }

    suspend fun getNovelChapterUrls(detailPageUrl: String): List<ChapterDto> =
        withContext(Dispatchers.IO) {
            val chapters = mutableListOf<ChapterDto>()
            val id = getNovelRemoteId(detailPageUrl)!!

            val response =
                Jsoup.connect("https://turkcelightnovels.com/wp-admin/admin-ajax.php")
                    .data("action", "manga_get_chapters")
                    .data(
                        "manga",
                        id
                    )
                    .post()

            val document: Document = Jsoup.parse(response.body().html())
            val chapterElements: Elements = document.select("li.wp-manga-chapter")

            for (chapter in chapterElements) {
                val url = chapter.selectFirst("a")?.attr("href").orEmpty()
                val title = chapter.selectFirst("a")?.text().orEmpty()
                val releaseDate =
                    chapter.selectFirst("span.chapter-release-date i")?.text().orEmpty()

                if (url.isNotEmpty() && title.isNotEmpty()) {
                    chapters.add(ChapterDto(title, releaseDate, url))
                }
            }
            return@withContext chapters
        }


    companion object {
        private const val TAG = "TurkceLightNovelScrapper"
    }

}
