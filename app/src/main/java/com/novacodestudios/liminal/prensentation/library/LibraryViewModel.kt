package com.novacodestudios.liminal.prensentation.library

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.data.worker.ChapterDownloadWorker
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.prensentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val seriesRepository: SeriesRepository,
    private val chapterRepository: ChapterRepository,
    private val workManager: WorkManager,
    private val filesDir: File,
) : ViewModel() {
    var state by mutableStateOf(LibraryState())
        private set

    private val _eventFlow = MutableSharedFlow<UIState>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getSeriesList()
    }

    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnResetSeries -> resetSeries(event.series)
            is LibraryEvent.OnDownloadSeries -> downloadSeries(event.series)
            is LibraryEvent.OnSeriesItemClicked -> navigateReadingScreen(event.series)
        }
    }

    private fun navigateReadingScreen(series: Series) {
        viewModelScope.launch {
            _eventFlow.emit(
                UIState.NavigateReading(
                    seriesType = series.type,
                    seriesId = series.currentChapterId
                )
            )
        }
    }

    // TODO: sadece wifi ya izin ver veya kullanıcı isterse mobil veriye izin ver
    @SuppressLint("RestrictedApi")
    private fun downloadSeries(series: Series) {
        Log.d(TAG, "downloadSeries: download clicked")
        viewModelScope.launch {
            val activeWorkers = workManager.getWorkInfosByTag(series.id).await()
                .any { it.state == WorkInfo.State.RUNNING }

            if (activeWorkers) {
                sendMessage("Bu seri için indirme işlemi zaten devam ediyor.")
                return@launch
            }

            val chapters = chapterRepository.getChaptersBySeriesId(series.id)
                .first()
                .filter { it.filePath == null }

            if (chapters.isEmpty()) {
                sendMessage("Bu seri zaten indirilmiş")
                return@launch
            }

            val requests = chapters.map {
                val chapterId = it.id
                val inputData = workDataOf(ChapterDownloadWorker.CHAPTER_ID to chapterId)

                OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
                    .setInputData(inputData)
                    .addTag(series.id)
                    .build()
            }
            workManager.enqueue(requests)
            sendMessage("İndirme işlemi başladı.")
        }
    }

    private fun resetSeries(series: Series) {
        viewModelScope.launch {
            val chapters = chapterRepository.getChaptersBySeriesId(series.id)
                .first()
            deleteChaptersFromLocale(chapters)

            seriesRepository.deleteSeriesById(series.id)
        }

    }

    private fun deleteChaptersFromLocale(chapterList: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            chapterList.forEach { chapter ->
                val path = chapter.filePath
                if (!path.isNullOrEmpty()) {
                    val chapterDirectory = File(filesDir, path)

                    if (chapterDirectory.exists() && chapterDirectory.isDirectory) {
                        try {
                            chapterDirectory.deleteRecursively()
                            Log.d(
                                TAG,
                                "deleteChaptersFromLocale: Silindi - ${chapterDirectory.absolutePath}"
                            )
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "deleteChaptersFromLocale: Hata oluştu - ${chapterDirectory.absolutePath}",
                                e
                            )
                        }
                    } else {
                        Log.w(
                            TAG,
                            "deleteChaptersFromLocale: Klasör bulunamadı - ${chapterDirectory.absolutePath}"
                        )
                    }
                } else {
                    Log.w(TAG, "deleteChaptersFromLocale: Geçersiz path - Chapter : ${chapter}")
                }
            }

            withContext(Dispatchers.Main) {
                sendMessage("Yerel dosyalardaki mangalar silindi")
            }
        }
    }


    private fun getSeriesList() {
        viewModelScope.launch {
            seriesRepository.getAllSeries()
                .collectLatest { series ->
                    state =
                        state.copy(seriesList = series.sortedByDescending { it.lastReadingDateTime })
                }
        }
    }

    sealed interface UIState {
        data class ShowSnackBar(val message: UiText) : UIState
        data class NavigateReading(val seriesType: SeriesType, val seriesId: String) : UIState
    }

    // TODO: kaldır
    private suspend fun sendMessage(message: String) {
        _eventFlow.emit(UIState.ShowSnackBar(UiText.DynamicString(message)))
    }

    companion object {
        private const val TAG = "LibraryViewModel"
    }
}

data class LibraryState(
    val isLoading: Boolean = false,
    val seriesList: List<Series> = emptyList(),
    val selectedChapterList: List<Chapter> = emptyList(),
    val selectedChapter: Chapter? = null,
)

sealed class LibraryEvent {
    data class OnResetSeries(val series: Series) : LibraryEvent()
    data class OnDownloadSeries(val series: Series) : LibraryEvent()
    data class OnSeriesItemClicked(val series: Series) : LibraryEvent()
}


