package com.novacodestudios.liminal.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.novacodestudios.liminal.R
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class DownloadWorker2 @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mangaRepository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val seriesId = inputData.getString(SERIES_ID) ?: return Result.failure()
        createNotificationChannel(applicationContext)

        return try {
            val series = seriesRepository.getSeriesByIdNew(seriesId)
            val seriesPath = setupSeriesPath(series)
            val chapters = chapterRepository.getChaptersNew(seriesId).first()

            Log.d(TAG, "doWork: chapters: $chapters")
            val filteredChapters =
                chapters.filter { it.downloadChapterPath == null }
            if (filteredChapters.isEmpty()) {
                Log.d(TAG, "doWork: filtered chapters is empty all chapter downloaded")
                showCompletionNotification(seriesName = series.name, true)
                return Result.success()
            }
            setForegroundAsync(createForegroundInfo(series.name))

            downloadChapters(filteredChapters, seriesPath)

            showCompletionNotification(seriesName = series.name, true)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork failed", e)
            showCompletionNotification(seriesName = "Manga", false)
            Result.failure()
        }
    }

    private fun createForegroundInfo(seriesName: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "download_channel")
            .setContentTitle("İndiriliyor: $seriesName")
            .setContentText("İndirme işlemi başlatıldı...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, 0, true)
            .build()

        return ForegroundInfo(1, notification)
    }

    // TODO: Bunu flowa bağla indirilen sayfaları dinamik olark takip etsin
    private fun updateNotification(
        seriesName: String,
        chapterIndex: Int,
        totalChapters: Int,
        imageIndex: Int,
        totalImages: Int
    ) {
        val progress =
            ((chapterIndex + imageIndex.toDouble() / totalImages) / totalChapters * 100).toInt()

        val notification = NotificationCompat.Builder(applicationContext, "download_channel")
            .setContentTitle("İndiriliyor: $seriesName")
            .setContentText("Bölüm ${chapterIndex + 1} / $totalChapters")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: İkonu değiştir
            .setProgress(100, progress, false)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private suspend fun setupSeriesPath(series: SeriesEntity): String {
        val seriesPath = "Manga/${series.name}"
        seriesRepository.upsert(series.copy(downloadedContentPath = seriesPath))
        return seriesPath
    }

    private suspend fun downloadChapters(chapters: List<ChapterEntity>, seriesPath: String) {
        chapters.forEachIndexed { chapterIndex, chapter ->
            val chapterPath = "$seriesPath/${chapter.title}"
            chapterRepository.updateChapterDownloadPath(chapter.id, chapterPath)

            val imageUrls = mangaRepository.getMangaImageUrlsNew(chapter.url).first()
            imageUrls.forEachIndexed { imageIndex, _ ->
                updateNotification(
                    seriesName = seriesPath.substringAfter("/"),
                    chapterIndex,
                    chapters.size,
                    imageIndex,
                    imageUrls.size
                )
            }

            downloadImages(imageUrls, chapterPath, chapter)
        }
    }

    private suspend fun downloadImages(
        urls: List<String>,
        chapterPath: String,
        chapterEntity: ChapterEntity
    ) {
        coroutineScope {
            val downloadJobs = urls.mapIndexed { index, url ->
                Log.d(
                    TAG,
                    "downloadImages: ${chapterEntity.title} sayfa ${index + 1} İndiriliyor... "
                )
                async {
                    downloadImage(url, chapterPath, index + 1)
                }
            }

            val results = downloadJobs.awaitAll()
            if (results.contains(false)) {
                chapterRepository.upsertChapter(chapterEntity.copy(downloadChapterPath = null))
                throw IOException("download fail")
            }
            Log.d(
                TAG,
                "downloadImages: ================================ ${chapterEntity.title} İndirildi ================================"
            )
        }

    }


    private suspend fun downloadImage2(url: String, chapterPath: String, pageNumber: Int): Boolean {
        Log.d(TAG, "downloadImage: chapterPath: $chapterPath")
        val request = Request.Builder().url(url).build()
        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val fileExtension = response.body?.contentType()?.subtype ?: "jpg"
                val directory = File(applicationContext.filesDir, chapterPath).apply { mkdirs() }
                val file = File(directory, "page$pageNumber.$fileExtension")

                response.body?.byteStream()?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                true
            } catch (e: Exception) { // TODO: buraya retry meanizması ekle
                File(applicationContext.filesDir, "$chapterPath/page$pageNumber.jpg").delete()
                Log.e(TAG, "downloadImage: görsel indirilirken hata oluştu", e)
                false
            }
        }
    }

    private suspend fun downloadImage(url: String, chapterPath: String, pageNumber: Int): Boolean {
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
                true
            } catch (e: Exception) {
                File(applicationContext.filesDir, "$chapterPath/page$pageNumber.jpg").delete()
                Log.e(TAG, "downloadImage: Error downloading image", e)
                false
            }
        }
    }


    private fun showCompletionNotification(seriesName: String, success: Boolean) {
        val message = if (success) {
            "$seriesName indirildi!"
        } else {
            "$seriesName indirme başarısız."
        }

        val notification = NotificationCompat.Builder(applicationContext, "download_channel")
            .setContentTitle(message)
            //.setSmallIcon(if (success) R.drawable.ic_check else R.drawable.ic_error)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }


    companion object {
        private const val TAG = "DownloadWorker"
        private val client = OkHttpClient()
        const val SERIES_ID = "seriesId"
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "download_channel",
            "İndirme Bildirimleri",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Manga indirme işlemleri için bildirimler"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}