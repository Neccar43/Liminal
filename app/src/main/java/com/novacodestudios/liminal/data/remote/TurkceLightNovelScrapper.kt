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
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.DocElement
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.h3
import it.skrape.selects.html5.img
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import kotlin.coroutines.resume
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

// TODO: diğer scraper lar için linkler https://novelaozora.com/manga/issizin-reenkarnasyonu/

// TODO: https://namevt.com/mushoku-tensei-ln-onceki-bolumleri-okuma-bilgilendirmesi/ 

class TurkceLightNovelScrapper(private val context: Context) {

    suspend fun getNovelList(): List<NovelPreviewDto> {
        return skrape(HttpFetcher) {
            request {
                url = "https://turkcelightnovels.com"
                timeout=60_000
            }
            response {
                htmlDocument {
                    div {
                        withClass = "page-item-detail"
                        findAll {
                            map {
                                NovelPreviewDto(
                                    name = it.h3 {
                                        withClass = "h5"
                                        findFirst { text }
                                    },
                                    imageUrl = it.img { findFirst { attribute("data-src") } },

                                    detailPageUrl = it.h3 {
                                        withClass = "h5"
                                        a {
                                            findFirst {
                                                eachHref.first()
                                            }
                                        }

                                    },
                                    source = "turkcelightnovels"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun getNovelDetail(detailPageUrl: String): NovelDetailDto {
        val detail = skrape(HttpFetcher) {
            request {
                url = detailPageUrl
                timeout=60_000
            }
            response {
                NovelDetailDto(
                    name = document.div {
                        withClass = "post-title"
                        h1 { findFirst { text } }
                    },
                    imageUrl = document.div {
                        withClass = "summary_image"
                        img { findFirst { attribute("data-src") } }
                    },
                    author = document.div {
                        withClass = "author-content"
                        a { findFirst { text } }
                    },

                    rate = document.span {
                        withClass = "score"
                        findFirst { text }
                    },
                    summary = document.div {
                        withClass = "summary__content"
                        findFirst { text }
                    },
                    chapters = emptyList()
                )
            }
        }
        return detail

    }

    //okhttp çözümü 1296 917 904 1952
    suspend fun getNovelChapterContent(chapterUrl: String): List<String> {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(chapterUrl)
            .build()

        val response = client.newCall(request).execute()
        val htmlContent = response.body?.string() ?: ""

        return parseHtmlContent(htmlContent)
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
