package com.novacodestudios.liminal.prensentation.mangaReading

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.mapper.toChapterList
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.navigation.NavArguments
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.getNextChapter
import com.novacodestudios.liminal.util.getPreviousChapter
import com.novacodestudios.liminal.util.hashToMD5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MangaReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {
    var state by mutableStateOf(MangaState())
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            NavArguments.currentChapter?.let { chapter ->
                val seriesEntity = getSeriesEntity(chapter = chapter)
                state = state.copy(seriesEntity = seriesEntity)
                if (state.seriesEntity != null) {
                    val chapters = NavArguments.chapterList.reversed()

                    Log.d(TAG, "init: chapter: $chapter, chapters: $chapters ")
                    state = state.copy(currentChapter = chapter, chapters = chapters)

                    getMangaImages(chapter, isNextChapter = null)
                }


            }
        }


    }

    // TODO: view modelde susupend fonksiyon olması sakıncalı mı bunu araştır
    private suspend fun getSeriesEntity(chapter: Chapter): SeriesEntity {
        val id = chapter.url.hashToMD5()
        return seriesRepository.getSeriesByChapterId(id)

    }


    private fun getChapters(seriesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chapterRepository.getChapters(seriesId).collectLatest {
                when (it) {
                    is Resource.Error -> {}
                    Resource.Loading -> {}
                    is Resource.Success -> {
                        state = state.copy(chapters = it.data.toChapterList())
                    }
                }
            }
        }
    }

    private fun getMangaImages(chapter: Chapter, isNextChapter: Boolean?) {
        val isDownloaded = chapter.filePath != null
        if (isDownloaded) {
            getMangaImagesFromLocale(chapter, isNextChapter = isNextChapter)
            return
        }
        getMangaImagesFromRemote(chapter.url, isNextChapter = isNextChapter)
    }

    private fun getMangaImagesFromRemote(
        chapterUrl: String,
        isNextChapter: Boolean?
    ) { // TODO: Refactor et
        Log.d(TAG, "getMangaImagesFromRemote: çalışıyor")
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMangaImageUrls(chapterUrl).collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            state = state.copy(isLoading = false)
                            _eventFlow.emit(
                                UIEvent.ShowToast(
                                    resource.exception.localizedMessage ?: "An error occurred"
                                )
                            )
                        }

                        Resource.Loading -> state = state.copy(isLoading = true)
                        is Resource.Success -> {
                            Log.d(TAG, "getMangaImages: ${resource.data}")
                            if (isNextChapter == null) {
                                state =
                                    state.copy(imageSources = resource.data, isLoading = false)
                                return@withContext
                            }

                            if (isNextChapter) {
                                state = state.copy(
                                    isLoading = false,
                                    imageSources = resource.data,
                                    seriesEntity = state.seriesEntity!!.copy(currentPageIndex = 0)
                                )
                            } else {
                                state = state.copy(
                                    isLoading = false,
                                    imageSources = resource.data,
                                    seriesEntity = state.seriesEntity!!.copy(currentPageIndex = resource.data.size - 1)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMangaImagesFromLocale(chapter: Chapter, isNextChapter: Boolean?) {
        Log.d(TAG, "getMangaImagesFromLocale: çalışıyor")
        state = state.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            val path = chapter.filePath!!

            val imagesDirectory = File(NavArguments.filesDir!!, path)

            if (imagesDirectory.exists() && imagesDirectory.isDirectory) {
                val imageFiles =
                    imagesDirectory.listFiles { file ->
                        file.isFile
                    }?.toList()?.sortedBy { file ->
                        val pageNumber = file.nameWithoutExtension
                            .replace(Regex("[^0-9]"), "")
                            .toIntOrNull()
                            ?: Int.MAX_VALUE // Sayıya dönüştürülmeyenler sonlara atılır
                        pageNumber
                    } ?: emptyList()

                Log.d(TAG, "getMangaImagesFromLocale: imageFiles: $imageFiles")

                withContext(Dispatchers.Main) {

                    Log.d(TAG, "getMangaImagesFromLocale: seriesEntity: ${state.seriesEntity}")
                    state = if (isNextChapter == null) {
                        state.copy(imageSources = imageFiles, isLoading = false)
                    } else if (isNextChapter) {
                        state.copy(
                            isLoading = false,
                            imageSources = imageFiles,
                            seriesEntity = state.seriesEntity!!.copy(currentPageIndex = 0)
                        )
                    } else {
                        state.copy(
                            isLoading = false,
                            imageSources = imageFiles,
                            seriesEntity = state.seriesEntity!!.copy(currentPageIndex = imageFiles.size - 1)
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: MangaEvent) {
        when (event) {
            is MangaEvent.OnReadingModeChanged -> {
                state = state.copy(readingMode = event.readingMode)
            }

            MangaEvent.OnNextChapter -> {
                getNextChapter()
            }

            MangaEvent.OnPreviousChapter -> {
                getPreviousChapter()
            }

            is MangaEvent.OnPageChange -> {
                Log.d(TAG, "onEvent: page index: ${event.pageIndex}")
                updateSeriesCurrentChapterAndIndex(
                    chapter = state.currentChapter,
                    pageIndex = event.pageIndex
                )
            }
        }
    }

    private fun getNextChapter() {
        Log.d(TAG, "getNextChapter: çalıştı")
        val currentChapter = state.currentChapter
        Log.d(TAG, "getNextChapter: currentChapter $currentChapter")
        setChapterIsReadTrue(currentChapter)
        Log.d(TAG, "getNextChapter: chapters: ${state.chapters}")
        val nextChapter = state.chapters.getNextChapter(currentChapter)
        Log.d(TAG, "getNextChapter: nextChapter $nextChapter")
        nextChapter ?: return
        updateSeriesCurrentChapterAndIndex(nextChapter, 0)
        state = state.copy(currentChapter = nextChapter)
        getMangaImages(nextChapter, isNextChapter = true)

    }

    private fun setChapterIsReadTrue(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = chapter.url.hashToMD5()
            Log.d(TAG, "setChapterIsReadTrue: id: $id")
            chapterRepository.setIsReadByChapterId(id, isRead = true).collectLatest {
                when (it) {
                    is Resource.Error -> {
                        Log.e(TAG, "setChapterIsReadTrue: hata", it.exception)
                    }

                    Resource.Loading -> {
                        Log.d(TAG, "setChapterIsReadTrue: loading")
                    }

                    is Resource.Success -> {
                        Log.d(TAG, "setChapterIsReadTrue: success")
                    }
                }
            }
        }
    }

    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        getMangaImages(state.currentChapter, isNextChapter = false)
    }

    private fun updateSeriesCurrentChapterAndIndex(chapter: Chapter, pageIndex: Int) {
        viewModelScope.launch {
            val seriesEntity = state.seriesEntity
            Log.d(TAG, "updateSeriesCurrentChapterAndIndex: seriesEntity: $seriesEntity")
            if (seriesEntity != null) {
                val chapterId = chapter.url.hashToMD5()
                val seriesEntity = seriesEntity.copy(
                    lastReadingDateTime = System.currentTimeMillis(),
                    currentChapterId = chapterId,
                    currentChapterName = chapter.title,
                    currentPageIndex = pageIndex
                )
                Log.d(
                    TAG,
                    "updateSeriesCurrentChapterAndIndex: index: $pageIndex series: $seriesEntity"
                )
                seriesRepository.upsert(seriesEntity).collectLatest { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            Log.e(
                                TAG,
                                "updateSeriesCurrentChapterAndIndex: Hata: ${resource.exception}"
                            )
                        }

                        Resource.Loading -> {}
                        is Resource.Success -> {
                            Log.d(TAG, "updateSeriesCurrentChapterAndIndex: Index değiştirldi")
                        }
                    }

                }

                return@launch
            }

        }
    }


    companion object {
        private const val TAG = "MangaReadingViewModel"
    }

    sealed class UIEvent {
        data class ShowToast(val message: String) : UIEvent()
    }


}

data class MangaState(
    val isLoading: Boolean = false,
    val currentChapter: Chapter = Chapter("", "", "", null),
    val chapters: List<Chapter> = emptyList(),
    val imageSources: List<Any> = emptyList(),
    val readingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
    val seriesEntity: SeriesEntity? = null,
)

enum class ReadingMode(val modeName: String) {
    WEBTOON("Webtoon"),
    LEFT_TO_RIGHT("Left to right"),
    RIGHT_TO_LEFT("Right to left")
}

sealed class MangaEvent {
    data class OnReadingModeChanged(val readingMode: ReadingMode) : MangaEvent()
    data object OnNextChapter : MangaEvent()
    data object OnPreviousChapter : MangaEvent()
    data class OnPageChange(val pageIndex: Int) : MangaEvent()
}

