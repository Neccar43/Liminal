package com.novacodestudios.liminal.data.remote

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.RecentMangaChaptersDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import it.skrape.core.document
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachSrc
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h2
import it.skrape.selects.html5.img
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class SadScansScrapper @Inject constructor(private val context: Context):MangaScraper {
    override val baseUrl: String
        get() = "https://sadscans.com/"

    override suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto {
        return skrape(HttpFetcher) {
            request {
                url = detailPageUrl
            }

            response {
                MangaDetailDto(
                    name = document.div {
                        withClass = "title"
                        h2 { findFirst { text } }
                    },
                    imageUrl = baseUrl + document.div {
                        withClass = "series-image"
                        img { findFirst { attribute("src") } }
                    },
                    summary = document.div {
                        withClass = "summary"
                        p { findFirst { text } }
                    },
                    author = document.div {
                        withClass = "author"
                        span { findSecond { text } }
                    },
                    chapters = emptyList(),
                )
            }
        }
    }

    override suspend fun getMangaChapterImages(chapterUrl: String): List<String> {
        return skrape(HttpFetcher) {
            request {
                url = chapterUrl
            }

            response {
                htmlDocument {
                    img {
                        findAll {
                            eachSrc.filter { it.startsWith("htt") }
                        }
                    }
                }

            }
        }
    }

    override suspend fun getMangaList(pageNumber: Int): List<MangaPreviewDto> {
        return skrape(HttpFetcher) {
            request {
                url = baseUrl+"series"
            }

            response {
                htmlDocument {
                    div {
                        withClass = "series-list"
                        findAll {
                            map {
                                MangaPreviewDto(
                                    name = it.h2 { findFirst { text } },
                                    imageUrl = baseUrl+it.img { findFirst { attribute("data-src") } },
                                    detailPageUrl = baseUrl + it.a {
                                        withClass = "button"
                                        findFirst { eachHref.first() }
                                    },
                                    source = "sadscans"
                                )
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
                    withClass = "chap"
                    div {
                        withClass = "link"
                        findAll {
                            map {
                                ChapterDto(
                                    title = it.a { findFirst { text } },
                                    url = baseUrl + it.a { findFirst { eachHref.first() } },
                                    releaseDate = "-"
                                )
                            }
                        }
                    }

                }
            }
        }
    }


    suspend fun getRecentMangaChapters(): List<RecentMangaChaptersDto> {
        return skrape(HttpFetcher) {
            request {
                url = baseUrl
            }

            response {
                htmlDocument {
                    div {
                        withClass = "chap-content"

                        findAll {
                            map { docElement ->
                                RecentMangaChaptersDto(
                                    name = docElement.findFirst { attribute("title") },
                                    imageUrl = "", // TODO: Resimleri çek
                                    chapters = docElement.div {
                                        withClass = "chap-nav"
                                        findFirst {
                                            a {
                                                withClass = "chap-info"
                                                findAll {
                                                    map {
                                                        ChapterDto(
                                                            title = it.attribute("title"),
                                                            releaseDate = it.findFirst("span.chap-date") { text },
                                                            url = baseUrl + it.attribute(
                                                                "href"
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    companion object{
        private const val TAG = "SadScansScrapper"
    }
}