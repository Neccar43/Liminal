package com.novacodestudios.liminal.data.remote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.NovelDetailDto
import com.novacodestudios.liminal.data.remote.dto.NovelPreviewDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.coroutines.resume

// TODO: diğer scraper lar için linkler https://novelaozora.com/manga/issizin-reenkarnasyonu/

// TODO: https://namevt.com/mushoku-tensei-ln-onceki-bolumleri-okuma-bilgilendirmesi/ 

class TurkceLightNovelScrapper(private val context: Context) {

    // TODO: bu fonksiyon haricnde diğerlerinde sıkıntı var bunu düzelt
    suspend fun getNovelList(): List<NovelPreviewDto> = withContext(Dispatchers.IO){
        val document: Document = Jsoup.connect("https://turkcelightnovels.com")
            //.timeout(60_000)  // Timeout ayarını belirt
            .get()

         document.select("div.page-item-detail").map { element ->
            NovelPreviewDto(
                name = element.selectFirst("h3.h5")?.text() ?: "",
                imageUrl = element.selectFirst("img")?.attr("data-src") ?: "",
                detailPageUrl = element.selectFirst("h3.h5 a")?.attr("href")
                    ?.let { "https://turkcelightnovels.com$it" } ?: "",
                source = "turkcelightnovels"
            )
        }
    }


    suspend fun getNovelDetail(detailPageUrl: String): NovelDetailDto = withContext(Dispatchers.IO){
        val document: Document = Jsoup.connect(detailPageUrl)
            //.timeout(60_000)  // Timeout ayarını belirt
            .get()

         NovelDetailDto(
            name = document.selectFirst("div.post-title h1")?.text() ?: "",
            imageUrl = document.selectFirst("div.summary_image img")?.attr("data-src") ?: "",
            author = document.selectFirst("div.author-content a")?.text() ?: "",
            rate = document.selectFirst("span.score")?.text() ?: "",
            summary = document.selectFirst("div.summary__content")?.text() ?: "",
            chapters = emptyList()
        )
    }

    suspend fun getNovelChapterContent(chapterUrl: String): List<String> = withContext(Dispatchers.IO){
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

    suspend fun getNovelChapterUrls(
        detailPageUrl: String
    ): List<ChapterDto> {
        return suspendCancellableCoroutine { continuation ->
            Handler(Looper.getMainLooper()).post {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            Log.d(TAG, "onPageFinished: $url")
                            view?.evaluateJavascript(
                                """
                    (function() {
                        var chapters = document.querySelectorAll('ul.main.version-chap.no-volumn.active li.wp-manga-chapter');
                        var chapterList = [];
                        chapters.forEach(function(chapter) {
                            var title = chapter.querySelector('a').innerText.trim();
                            var url = chapter.querySelector('a').href;
                            var releaseDate = chapter.querySelector('.chapter-release-date').innerText.trim();
                            chapterList.push({ title: title, releaseDate: releaseDate, url: url });
                        });
                        return JSON.stringify(chapterList);
                    })();
                    """
                            ) { result ->
                                val chapterDtos = parseChapterJson(result)
                                Log.d(TAG, "onPageFinished: chapters: $chapterDtos")
                                continuation.resume(chapterDtos)
                            }
                        }
                    }
                    loadUrl(detailPageUrl)
                }
            }
        }
    }

    private fun parseChapterJson(jsonString: String?): List<ChapterDto> {
        val chapterList = mutableListOf<ChapterDto>()
        jsonString?.let { json ->
            val jsonArray = json.removeSurrounding("[", "]")
                .replace("\\\"", "\"")
                .trim()
            if (jsonArray.isNotEmpty()) {
                val chapters = jsonArray.split("},{")
                chapters.forEach { chapterStr ->
                    val title = getValueForKey(chapterStr, "title")
                    val releaseDate = getValueForKey(chapterStr, "releaseDate")
                    val url = getValueForKey(chapterStr, "url")
                    if (title.isNotEmpty() && releaseDate.isNotEmpty() && url.isNotEmpty()) {
                        chapterList.add(ChapterDto(title, releaseDate, url))
                    }
                }
            }
        }
        return chapterList
    }

    private fun getValueForKey(chapterStr: String, key: String): String {
        val regex = """"$key":"(.*?)""""
        val matchResult = regex.toRegex().find(chapterStr)
        return matchResult?.groupValues?.get(1) ?: ""
    }

    companion object {
        private const val TAG = "TurkceLightNovelScrapper"
    }

}
