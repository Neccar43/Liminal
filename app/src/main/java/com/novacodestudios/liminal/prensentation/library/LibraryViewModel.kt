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
import com.novacodestudios.liminal.data.locale.entity.ChapterEntity
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.mapper.toChapter
import com.novacodestudios.liminal.domain.mapper.toChapterList
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.navigation.NavArguments
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.worker.ChapterDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    private val workManager: WorkManager
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
            is LibraryEvent.OnResetSeries -> resetSeries(event.seriesEntity)
            is LibraryEvent.OnDownloadSeries -> downloadSeries(event.seriesEntity)
            is LibraryEvent.OnSeriesItemClicked -> setSelectedChapters(event.seriesEntity)
        }
    }

    private fun setSelectedChapters(seriesEntity: SeriesEntity) {
        viewModelScope.launch {
            chapterRepository.getChapters(seriesEntity.id).handleResource { chapterEntityList ->
                state = state.copy(selectedChapterList = chapterEntityList.toChapterList())
                chapterRepository.getChapter(seriesEntity.currentChapterId)
                    .handleResource { chapterEntity ->
                        // TODO: Row daki on clickten ötürü resetleme sırasında burası da çalışıyor ve null geliyor
                        if (chapterEntity == null) return@handleResource
                        Log.d(
                            TAG,
                            "setSelectedChapters:İşlemler başarılı chapter entity: $chapterEntity list: $chapterEntityList"
                        )
                        state = state.copy(selectedChapter = chapterEntity.toChapter())
                        if (state.selectedChapter != null && state.selectedChapterList.isNotEmpty()) {
                            _eventFlow.emit(UIState.NavigateReadingScreen(seriesEntity))
                        } else {
                            _eventFlow.emit(UIState.ShowSnackBar("Chapter yüklenirken bir hata oluştu."))
                        }
                    }
            }
        }
    }

    // TODO: sadece wifi ya izin ver veya kullanıcı isterse mobil veriye izin ver
    @SuppressLint("RestrictedApi")
    private fun downloadSeries(seriesEntity: SeriesEntity) {
        Log.d(TAG, "downloadSeries: download clicked")
        viewModelScope.launch {
            val activeWorkers = workManager.getWorkInfosByTag(seriesEntity.id).await()
                .any { it.state == WorkInfo.State.RUNNING }

            if (activeWorkers) {
                _eventFlow.emit(UIState.ShowSnackBar("Bu seri için indirme işlemi zaten devam ediyor."))
                return@launch
            }

            val chapters = chapterRepository.getChaptersNew(seriesEntity.id)
                .first()
                .filter { it.downloadChapterPath == null }

            if (chapters.isEmpty()) {
                _eventFlow.emit(UIState.ShowSnackBar("Bu seri zaten indirilmiş"))
                return@launch
            }

            val requests = chapters.map {
                val chapterId = it.id
                val inputData = workDataOf(ChapterDownloadWorker.CHAPTER_ID to chapterId)

                OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
                    .setInputData(inputData)
                    .addTag(seriesEntity.id)
                    .build()
            }
            workManager.enqueue(requests)
            _eventFlow.emit(UIState.ShowSnackBar("İndirme işlemi başladı."))
        }
    }

    private fun resetSeries(series: SeriesEntity) {
        chapterRepository.getChapters(series.id).handleResource { chapters ->
            deleteChaptersFromLocale(chapters)

        }
        viewModelScope.launch {
            seriesRepository.deleteSeries(series).handleResource {
                getSeriesList()
            }
        }
    }

    private fun deleteChaptersFromLocale(chapterList: List<ChapterEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            chapterList.forEach { chapter ->
                val path = chapter.downloadChapterPath
                if (!path.isNullOrEmpty()) {
                    val chapterDirectory = File(NavArguments.filesDir!!, path)

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
                _eventFlow.emit(UIState.ShowSnackBar("Yerel dosyalardaki mangalar silindi"))
            }
        }
    }


    private fun getSeriesList() {
        viewModelScope.launch {
            seriesRepository.getAllSeries().handleResource { seriesEntityList ->
                state = state.copy(seriesEntityList = seriesEntityList)
            }
        }
    }

    sealed class UIState {
        data class ShowSnackBar(val message: String) : UIState()
        data class NavigateReadingScreen(val seriesEntity: SeriesEntity) : UIState()
    }

    private fun <T> Flow<Resource<T>>.handleResource(
        onSuccess: suspend (T) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            this@handleResource.collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            state = state.copy(isLoading = false)
                            _eventFlow.emit(
                                UIState.ShowSnackBar(
                                    resource.exception.localizedMessage ?: "An error occurred"
                                )
                            )
                        }

                        Resource.Loading -> {
                            state = state.copy(isLoading = true)
                        }

                        is Resource.Success -> {
                            state = state.copy(isLoading = false)
                            onSuccess(resource.data)
                        }
                    }
                }

            }
        }
    }

    companion object {
        private const val TAG = "LibraryViewModel"
    }
}

data class LibraryState(
    val isLoading: Boolean = false,
    val seriesEntityList: List<SeriesEntity> = emptyList(),
    val selectedChapterList: List<Chapter> = emptyList(),
    val selectedChapter: Chapter? = null,
)

sealed class LibraryEvent {
    data class OnResetSeries(val seriesEntity: SeriesEntity) : LibraryEvent()
    data class OnDownloadSeries(val seriesEntity: SeriesEntity) : LibraryEvent()
    data class OnSeriesItemClicked(val seriesEntity: SeriesEntity) : LibraryEvent()
}


