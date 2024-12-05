package com.novacodestudios.liminal.data.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.Content
import com.novacodestudios.liminal.domain.util.map
import com.novacodestudios.liminal.domain.util.onError
import com.novacodestudios.liminal.domain.util.onSuccess
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

// TODO: Elden geçir
@HiltWorker
class ChapterDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val chapterId = inputData.getString(CHAPTER_ID) ?: return Result.failure()
        return try {
            val chapter = chapterRepository.getChapterById(chapterId)

            seriesRepository.getChapterContent(chapter.url)
                .map { it.filterIsInstance<Content.Image>().map { image -> image.url } }
                .onSuccess { imagesUrls ->
                    val series = seriesRepository.getSeriesByChapterId(chapterId)
                    val seriesPath = "Manga/${series.name}"
                    val chapterPath = "$seriesPath/${chapter.title}"

                    downloadImages(imagesUrls, chapterPath, chapter, seriesId = series.id)
                    chapterRepository.updateChapterDownloadPath(chapter.id, chapterPath)
                    return Result.success()
                }.onError {
                    Log.e(TAG, "doWork: error,$it")
                    Result.failure()
                }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork failed", e)
            chapterRepository.updateChapterDownloadPath(chapterId, null)
            Result.failure()
        }


    }

    private suspend fun downloadImages(
        urls: List<String>,
        chapterPath: String,
        chapter: Chapter,
        maxRetryCount: Int = 3,
        seriesId: String
    ) {
        coroutineScope {
            var remainingUrls = urls
            var retryCount = 0

            while (remainingUrls.isNotEmpty() && retryCount < maxRetryCount) {
                val downloadJobs = remainingUrls.mapIndexed { index, url ->
                    Log.d(
                        TAG,
                        "downloadImages: ${chapter.title} sayfa ${index + 1} İndiriliyor... Deneme: ${retryCount + 1}"
                    )
                    async {
                        downloadImage(url, chapterPath, index + 1)
                    }
                }

                val results = downloadJobs.awaitAll()
                val failedResults = results.filter { !it.isSuccess }

                remainingUrls = failedResults.map { it.url }
                retryCount++
            }

            if (remainingUrls.isNotEmpty()) {
                chapterRepository.upsertChapter(chapter.copy(filePath = null), seriesId, 0)
                Log.e(TAG, "downloadImages: Bazı sayfalar indirilemedi: $remainingUrls")
                throw IOException("Some images failed to download after $maxRetryCount retries.")
            }

            Log.d(
                TAG,
                "downloadImages: ================================ ${chapter.title} İndirildi ================================"
            )
        }
    }

    private suspend fun downloadImage(
        url: String,
        chapterPath: String,
        pageNumber: Int
    ): DownloadResult {
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val contentType = response.body?.contentType()
                if (contentType == null || contentType.type != "image") {
                    throw IOException("Unsupported MIME type: $contentType")
                }
                val fileExtension = contentType.subtype

                val directory = File(applicationContext.filesDir, chapterPath).apply { mkdirs() }
                val originalFile = File(directory, "page$pageNumber.$fileExtension")
                val jpegFile = File(directory, "page$pageNumber.jpg")

                response.body?.byteStream()?.use { input ->
                    FileOutputStream(originalFile).use { output -> input.copyTo(output) }
                }

                if (fileExtension != "jpg" && fileExtension != "jpeg") {
                    val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
                        ?: throw IOException("Failed to decode image: ${originalFile.absolutePath}")
                    FileOutputStream(jpegFile).use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    }
                    originalFile.delete()
                }
                DownloadResult(url, true)
            } catch (e: Exception) {
                File(applicationContext.filesDir, "$chapterPath/page$pageNumber.jpg").delete()
                Log.e(TAG, "downloadImage: Error downloading image", e)
                DownloadResult(url, false)
            }
        }
    }

    companion object {
        private const val TAG = "ChapterDownloadWorker"
        private const val TIME_OUT = 60L
        private val client = OkHttpClient.Builder()
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .build()
        const val CHAPTER_ID = "chapterId"
    }

}

data class DownloadResult(val url: String, val isSuccess: Boolean)