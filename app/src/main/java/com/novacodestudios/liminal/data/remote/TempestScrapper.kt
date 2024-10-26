package com.novacodestudios.liminal.data.remote

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.i
import it.skrape.selects.html5.img
import it.skrape.selects.html5.li
import it.skrape.selects.html5.p
import it.skrape.selects.html5.ul
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class TempestScrapper(private val context: Context) : MangaScraper {
    override val baseUrl: String
        get() = "https://tempestscans.net/"

    override suspend fun getMangaList(pageNumber: Int): List<MangaPreviewDto> {
        return skrape(HttpFetcher) {
            request {
                url = baseUrl + "manga/?page=$pageNumber"
            }
            response {
                htmlDocument {
                    div {
                        withClass = "listupd"
                        div {
                            withClass = "bsx"
                            findAll {
                                map {
                                    MangaPreviewDto(
                                        name = it.a { findFirst { attribute("title") } },
                                        imageUrl = it.img { findFirst { attribute("src") } },
                                        detailPageUrl = it.a { findFirst { attribute("href") } },
                                        source = "tempestfansub"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun getMangaChapterList(detailPageUrl: String): List<ChapterDto> {
        return skrape(HttpFetcher) {
            request {
                url = detailPageUrl
            }
            response {
                document.div {
                    withClass = "eplister"
                    withId = "chapterlist"

                    ul {
                        li {
                            findAll {
                                map {
                                    ChapterDto(
                                        title = it.a { findFirst { text } },
                                        url = it.a { findFirst { attribute("href") } },
                                        releaseDate = "-"
                                    )
                                }

                            }
                        }
                    }

                }
            }
        }
    }

    // TODO: Birkerede birden fazla istek atıyor
    override suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto {
        return skrape(HttpFetcher) {
            request {
                url = detailPageUrl
            }
            response {
                /*val type=document.div {
                    withClass = "imptdt"
                    a{findFirst{text}}
                }
                val isVerticalRead=type!="Manga"*/
                MangaDetailDto(
                    name = document.h1 {
                        withClass = "entry-title"
                        findFirst { text }
                    },
                    imageUrl = document.div {
                        withClass = "thumb"
                        img {
                            findFirst { attribute("src") }
                        }
                    },
                    summary = document.p {
                        findFirst { text }
                    },
                    author = document.div {
                        withClass = "imptdt"
                        i { findByIndex(2) { text } }
                    },
                    chapters = emptyList(),
                    //isVerticalRead = isVerticalRead
                )
            }
        }
    }

    override suspend fun getMangaChapterImages(chapterUrl: String): List<String> {
        return withContext(Dispatchers.Main) {
            Log.d(TAG, "getMangaChapterImages: $chapterUrl")
            suspendCancellableCoroutine { continuation ->
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
                            view?.evaluateJavascript(
                                """
                                   (function() {
                                       var imgs = document.getElementById('readerarea').getElementsByTagName('img');
                                       var srcList = [];
                                       for (var i = 0; i < imgs.length; i++) {
                                           srcList.push(imgs[i].src);
                                       }
                                       return JSON.stringify(srcList);
                                   })();
                                   """
                            ) { result ->
                                val imageUrls = result?.parseUrls() ?: emptyList()

                                Log.d(TAG, "onPageFinished: image urls: ${imageUrls}")
                                Log.d(TAG, "onPageFinished: result: ${result}")
                                continuation.resume(imageUrls)
                            }
                        }
                    }
                    loadUrl(chapterUrl)
                }

            }
        }

    }

    private fun String.parseUrls(): List<String> {
        val cleanedString = this
            .removeSurrounding("[", "]")
            .replace("\"", "")

        return cleanedString.split(",")
            .map {
                it.replace("]", "")
                    .replace("[", "")
                    .replace("\\", "")
            }
    }

    companion object {
        private const val TAG = "TempestScrapper"
    }
}



