package com.novacodestudios.liminal.data.remote

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.NovelDetailDto
import com.novacodestudios.liminal.data.remote.dto.NovelPreviewDto
import com.novacodestudios.liminal.domain.model.NovelDetail
import com.novacodestudios.liminal.domain.model.NovelPreview
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.h3
import it.skrape.selects.html5.img
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.hours

// TODO: diğer scraper lar için linkler https://novelaozora.com/manga/issizin-reenkarnasyonu/

// TODO: https://namevt.com/mushoku-tensei-ln-onceki-bolumleri-okuma-bilgilendirmesi/ 

class TurkceLightNovelScrapper(private val context: Context) {

    suspend fun getNovelList(): List<NovelPreviewDto> {
        return skrape(HttpFetcher) {
            request {
                url = "https://turkcelightnovels.com"
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
        val detail= skrape(HttpFetcher) {
            request {
                url = detailPageUrl
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
        val chapters=getNovelChapterUrls(detailPageUrl = detailPageUrl, context = context)

        return detail.copy(chapters = chapters)

    }

    // TODO: Novel bölümünün içindeki görselleri de göster
    suspend fun getNovelChapterContent(chapterUrl: String): List<String> {
        return skrape(HttpFetcher) {
            request {
                url = chapterUrl
            }
            response {
                htmlDocument {
                    div {
                        withClass = "text-left"
                        p {
                            findAll {
                                eachText
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun getNovelChapterUrls(
        context: Context,
        detailPageUrl: String
    ): List<ChapterDto> {
        return suspendCancellableCoroutine { continuation ->
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
