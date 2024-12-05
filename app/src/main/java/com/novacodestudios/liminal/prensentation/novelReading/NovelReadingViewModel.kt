package com.novacodestudios.liminal.prensentation.novelReading

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
import javax.inject.Inject

@HiltViewModel
class NovelReadingViewModel @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
    savedStateHandle: SavedStateHandle,
) :
    ViewModel() {
    var state by mutableStateOf(NovelReadingState())
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            val chapterId = savedStateHandle.toRoute<Screen.NovelReading>().chapterId
            val series = seriesRepository.getSeriesByChapterId(chapterId)
            state = state.copy(series = series)
            if (state.series != null) {
                val chapters = chapterRepository.getChaptersBySeriesId(state.series!!.id).first()
                val chapter = chapters.find { it.id == chapterId } ?: return@launch
                state = state.copy(currentChapter = chapter, chapters = chapters)
                getChapterContent(state.currentChapter.url)
            }
        }
    }

    fun onEvent(event: NovelEvent) {
        when (event) {
            NovelEvent.OnNextChapter -> getNextChapter()
            NovelEvent.OnPreviousChapter -> getPreviousChapter()
            NovelEvent.OnContentRetry -> getChapterContent(state.currentChapter.url)
        }
    }

    private fun getChapterContent(chapterUrl: String) {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            seriesRepository.getChapterContent(chapterUrl)
                .onSuccess {
                    val contentList =
                        it.filterIsInstance<Content.Text>().map { text -> text.content }
                    state = state.copy(isLoading = false, chapterContent = contentList)

                }.onError {
                    state = state.copy(isLoading = false, error = it.toUiText())
                    _eventFlow.emit(UIEvent.ShowSnackbar(it.toUiText()))
                }
        }
    }

    private fun getNextChapter() {
        viewModelScope.launch {
            chapterRepository.setIsReadByChapterId(state.currentChapter.id, isRead = true)
            val nextChapter = state.chapters.getNextChapter(state.currentChapter) ?: return@launch
            updateSeriesCurrentChapterAndIndex(nextChapter)
            state = state.copy(currentChapter = nextChapter)
            getChapterContent(state.currentChapter.url)
        }
    }

    private fun updateSeriesCurrentChapterAndIndex(chapter: Chapter) {
        viewModelScope.launch {
            val seriesEntity = state.series?.copy(currentPageIndex = 0)
            seriesRepository.upsert(seriesEntity!!, currentChapter = chapter)
        }
    }

    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        updateSeriesCurrentChapterAndIndex(previousChapter)
        getChapterContent(state.currentChapter.url)
    }


    sealed class UIEvent {
        data class ShowSnackbar(val message: UiText) : UIEvent()
    }

    companion object {
        private const val TAG = "NovelReadingViewModel"
    }
}


data class NovelReadingState(
    val chapters: List<Chapter> = emptyList(),
    val currentChapter: Chapter = Chapter("", "", "", ""),
    val chapterContent: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val series: Series? = null,
    val error: UiText? = null
)

sealed class NovelEvent {
    data object OnNextChapter : NovelEvent()
    data object OnPreviousChapter : NovelEvent()
    data object OnContentRetry : NovelEvent()
}