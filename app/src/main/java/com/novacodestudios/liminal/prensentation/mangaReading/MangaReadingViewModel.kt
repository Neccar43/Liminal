package com.novacodestudios.liminal.prensentation.mangaReading

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.Content
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.util.onError
import com.novacodestudios.liminal.domain.util.onSuccess
import com.novacodestudios.liminal.prensentation.navigation.Screen
import com.novacodestudios.liminal.prensentation.util.UiText
import com.novacodestudios.liminal.prensentation.util.getNextChapter
import com.novacodestudios.liminal.prensentation.util.getPreviousChapter
import com.novacodestudios.liminal.prensentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MangaReadingViewModel @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
    private val filesDir: File,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var state by mutableStateOf(MangaState())
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            val chapterId = savedStateHandle.toRoute<Screen.MangaReading>().chapterId
            val series = seriesRepository.getSeriesByChapterId(chapterId)
            state = state.copy(series = series)
            if (state.series != null) {
                val chapters = chapterRepository.getChaptersBySeriesId(state.series!!.id).first().reversed()
                val chapter = chapters.find { it.id == chapterId } ?: return@launch
                state = state.copy(currentChapter = chapter, chapters = chapters)
                getMangaImages(chapter, isNextChapter = null)
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
    ) {
        Log.d(TAG, "getMangaImagesFromRemote: çalışıyor")
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            seriesRepository.getChapterContent(chapterUrl)
                .onSuccess {
                    // TODO: tek iterasyonda hallet
                    val imageUrls = it.filterIsInstance<Content.Image>().map { image -> image.url }
                    state = state.copy(isLoading = false)
                    Log.d(TAG, "getMangaImages: $imageUrls")
                    if (isNextChapter == null) {
                        state =
                            state.copy(imageSources = imageUrls, isLoading = false)
                        return@onSuccess
                    }

                    state = if (isNextChapter) {
                        state.copy(
                            isLoading = false,
                            imageSources = imageUrls,
                            series = state.series!!.copy(currentPageIndex = 0)
                        )
                    } else {
                        state.copy(
                            isLoading = false,
                            imageSources = imageUrls,
                            series = state.series!!.copy(currentPageIndex = imageUrls.size - 1)
                        )
                    }

                }.onError {
                    state = state.copy(isLoading = false)
                    _eventFlow.emit(UIEvent.ShowSnackbar(it.toUiText()))
                }
        }
    }


    private fun getMangaImagesFromLocale(chapter: Chapter, isNextChapter: Boolean?) {
        Log.d(TAG, "getMangaImagesFromLocale: çalışıyor")
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            val path = chapter.filePath!!

            val imagesDirectory = File(filesDir, path)

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



                Log.d(TAG, "getMangaImagesFromLocale: seriesEntity: ${state.series}")
                state = if (isNextChapter == null) {
                    state.copy(imageSources = imageFiles, isLoading = false)
                } else if (isNextChapter) {
                    state.copy(
                        isLoading = false,
                        imageSources = imageFiles,
                        series = state.series!!.copy(currentPageIndex = 0)
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        imageSources = imageFiles,
                        series = state.series!!.copy(currentPageIndex = imageFiles.size - 1)
                    )
                }

            }
        }
    }

    fun onEvent(event: MangaEvent) {
        when (event) {
            is MangaEvent.OnReadingModeChanged -> {
                state = state.copy(readingMode = event.readingMode)
            }

            MangaEvent.OnNextChapter -> getNextChapter()

            MangaEvent.OnPreviousChapter -> getPreviousChapter()

            is MangaEvent.OnPageChange -> {
                Log.d(TAG, "onEvent: page index: ${event.pageIndex}")
                updateSeriesCurrentChapterAndIndex(
                    chapter = state.currentChapter,
                    pageIndex = event.pageIndex
                )
            }

            is MangaEvent.OnChapterChange -> {
                state = state.copy(currentChapter = event.chapter)
                getMangaImages(event.chapter, isNextChapter = null)
            }
        }
    }

    private fun getNextChapter() {
        viewModelScope.launch {
            Log.d(TAG, "getNextChapter: çalıştı")
            val currentChapter = state.currentChapter
            Log.d(TAG, "getNextChapter: currentChapter $currentChapter")
            chapterRepository.setIsReadByChapterId(currentChapter.id, isRead = true)
            Log.d(TAG, "getNextChapter: chapters: ${state.chapters}")
            val nextChapter = state.chapters.getNextChapter(currentChapter)
            Log.d(TAG, "getNextChapter: nextChapter $nextChapter")
            nextChapter ?: return@launch
            updateSeriesCurrentChapterAndIndex(nextChapter, 0)
            state = state.copy(currentChapter = nextChapter)
            getMangaImages(nextChapter, isNextChapter = true)
        }


    }


    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        getMangaImages(state.currentChapter, isNextChapter = false)
    }

    private fun updateSeriesCurrentChapterAndIndex(chapter: Chapter, pageIndex: Int) {
        viewModelScope.launch {
            val series = state.series
            Log.d(TAG, "updateSeriesCurrentChapterAndIndex: series: $series")
            if (series != null) {
                val series = series.copy(currentPageIndex = pageIndex)
                Log.d(
                    TAG,
                    "updateSeriesCurrentChapterAndIndex: index: $pageIndex series: $series"
                )
                seriesRepository.upsert(series, currentChapter = chapter)

                return@launch
            }

        }
    }


    companion object {
        private const val TAG = "MangaReadingViewModel"
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: UiText) : UIEvent()
    }


}

data class MangaState(
    val isLoading: Boolean = false,
    val currentChapter: Chapter = Chapter("", "", "", ""),
    val chapters: List<Chapter> = emptyList(),
    val imageSources: List<Any> = emptyList(),
    val readingMode: ReadingMode = ReadingMode.LEFT_TO_RIGHT,
    val series: Series? = null,
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
    data class OnChapterChange(val chapter:Chapter) : MangaEvent()
}

/*Series(
"", "", "", "", "", emptyList(), SeriesType.MANGA, Source.SADSCANS, "",
emptyList(), "",
)*/

