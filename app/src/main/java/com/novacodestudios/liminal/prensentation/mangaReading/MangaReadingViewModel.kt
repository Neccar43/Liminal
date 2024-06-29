package com.novacodestudios.liminal.prensentation.mangaReading

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.screen.Screen
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
import javax.inject.Inject

@HiltViewModel
class MangaReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MangaRepository,
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
) : ViewModel() {
    var state by mutableStateOf(
        MangaState(
            currentChapter = savedStateHandle.toRoute<Screen.MangaReading>().currentChapter,
            chapters = savedStateHandle.toRoute<Screen.MangaReading>().chapters.reversed()
        )
    )
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            getMangaImages(state.currentChapter.url)
        }
        viewModelScope.launch {
            getSeriesEntity()
        }

    }

    private fun getSeriesEntity() {
        viewModelScope.launch(Dispatchers.IO) {
            val chapterId = state.currentChapter.url.hashToMD5()
            seriesRepository.getSeriesByChapterId(chapterId).collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "getSeriesEntity: Hata:${resource.exception}")
                        }

                        Resource.Loading -> {}
                        is Resource.Success -> {
                            state = state.copy(seriesEntity = resource.data)
                        }
                    }
                }
            }
        }
    }


    private fun getMangaImages(chapterUrl: String) {
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
                            state =
                                state.copy(imageUrls = resource.data, isLoading = false)
                        }
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

            is MangaEvent.OnPageChange -> updateSeriesCurrentChapterAndIndex(
                chapter = state.currentChapter,
                pageIndex = event.pageIndex
            )
        }
    }

    private fun getNextChapter() {
        setChapterIsReadTrue(state.currentChapter)
        val nextChapter = state.chapters.getNextChapter(state.currentChapter) ?: return
        updateSeriesCurrentChapterAndIndex(nextChapter, 0)
        state = state.copy(currentChapter = nextChapter)
        getMangaImages(state.currentChapter.url)

    }

    private fun setChapterIsReadTrue(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = chapter.url.hashToMD5()
            chapterRepository.setIsReadByChapterId(id, isRead = true)
        }
    }

    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        getMangaImages(state.currentChapter.url)
    }

    private fun updateSeriesCurrentChapterAndIndex(chapter: Chapter, pageIndex: Int) {
        viewModelScope.launch() {
            val chapterId = chapter.url.hashToMD5()
            val seriesEntity = state.seriesEntity?.copy(
                lastReadingDateTime = System.currentTimeMillis(),
                currentChapterId = chapterId,
                currentChapterName = chapter.title,
                currentPageIndex = pageIndex
            )
            Log.d(
                TAG,
                "updateSeriesCurrentChapterAndIndex: index: $pageIndex series: $seriesEntity"
            )
            seriesRepository.upsert(seriesEntity!!).collectLatest { resource ->
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
        }
    }


    companion object {
        private const val TAG = "MangaReaderViewModel"
    }

    sealed class UIEvent {
        data class ShowToast(val message: String) : UIEvent()
    }


}

data class MangaState(
    val isLoading: Boolean = false,
    val currentChapter: Chapter,
    val chapters: List<Chapter>,
    val imageUrls: List<String> = emptyList(),
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

