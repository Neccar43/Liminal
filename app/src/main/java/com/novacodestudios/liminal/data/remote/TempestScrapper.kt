package com.novacodestudios.liminal.data.remote

import android.content.Context
import com.novacodestudios.liminal.data.remote.dto.ChapterDto
import com.novacodestudios.liminal.data.remote.dto.MangaDetailDto
import com.novacodestudios.liminal.data.remote.dto.MangaPreviewDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.TimeUnit

class TempestScrapper(private val context: Context) : MangaScraper {
    override val baseUrl: String
        get() = "https://tempestscans.net/"

    override suspend fun getMangaList(pageNumber: Int): List<MangaPreviewDto> {
        val url = baseUrl + "manga/?page=$pageNumber"
        val document: Document = Jsoup.connect(url).get()

        return document.select("div.bsx").map { element ->
            MangaPreviewDto(
                name = element.selectFirst("a")?.attr("title") ?: "",
                imageUrl = element.selectFirst("img")?.attr("src") ?: "",
                detailPageUrl = element.selectFirst("a")?.attr("href") ?: "",
                source = "tempestfansub"
            )
        }
    }

    override suspend fun getMangaChapterList(detailPageUrl: String): List<ChapterDto> {
        val document: Document = Jsoup.connect(detailPageUrl).get()

        return document.select("div#chapterlist.eplister ul li").map { element ->
            ChapterDto(
                title = element.selectFirst("a")?.text() ?: "",
                url = element.selectFirst("a")?.attr("href") ?: "",
                releaseDate = "-"
            )
        }
    }

    override suspend fun getMangaDetail(detailPageUrl: String): MangaDetailDto {
        val document: Document = Jsoup.connect(detailPageUrl).get()

        return MangaDetailDto(
            name = document.selectFirst("h1.entry-title")?.text() ?: "",
            imageUrl = document.selectFirst("div.thumb img")?.attr("src") ?: "",
            summary = document.selectFirst("p")?.text() ?: "",
            author = document.select("div.imptdt i").getOrNull(2)?.text() ?: "",
            chapters = emptyList()
        )
    }


    override suspend fun getMangaChapterImages(chapterUrl: String): List<String> = withContext(Dispatchers.IO) {
        // HTML sayfasını al
        val doc = Jsoup.connect(chapterUrl).get()

        // "ts_reader.run" içeren <script> etiketini bul
        val scriptElement = doc.select("script").firstOrNull { it.data().contains("ts_reader.run") }

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




    suspend fun getMangaChapterImages1(chapterUrl: String): List<String> =
        withContext(Dispatchers.IO) {

            // URL'i oluşturuyoruz
            val url: HttpUrl =
                "https://liminal-api.onrender.com/tempest/images".toHttpUrlOrNull()!!.newBuilder()
                    .addQueryParameter("chapterUrl", chapterUrl)
                    .build()

            val request = Request.Builder()
                .url(url)
                .build()

            // İsteği gönderiyoruz ve cevap alıyoruz
            val response: Response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Request failed with status code: ${response.code}")
            }

            // Yanıtı işliyoruz
            response.body?.let { responseBody ->
                val jsonResponse = responseBody.string()

                // Kotlin Serialization ile string listesi olarak parse etme
                val json = Json {
                    ignoreUnknownKeys = true
                } // Eğer JSON'da bilinmeyen anahtarlar varsa onları yoksay

                // Yanıtı List<String> olarak parse et
                return@let json.decodeFromString<List<String>>(jsonResponse)
            } ?: throw Exception("Response body is empty")
        }


    /*override suspend fun getMangaChapterImages(chapterUrl: String): List<String> {
        return withContext(Dispatchers.Main) {
            Log.d(TAG, "getMangaChapterImages: $chapterUrl")
            suspendCancellableCoroutine { continuation ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
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

    }*/

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


        private val client by lazy {
            OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Bağlantı için timeout (default: 10 saniye)
                .readTimeout(60, TimeUnit.SECONDS)     // Okuma için timeout (default: 10 saniye)
                .writeTimeout(60, TimeUnit.SECONDS).build()
        }
    }
}




