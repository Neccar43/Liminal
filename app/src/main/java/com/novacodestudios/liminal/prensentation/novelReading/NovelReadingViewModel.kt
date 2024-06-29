package com.novacodestudios.liminal.prensentation.novelReading

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
import com.novacodestudios.liminal.data.repository.NovelRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.mapper.toChapter
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.screen.Screen
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.getNextChapter
import com.novacodestudios.liminal.util.getPreviousChapter
import com.novacodestudios.liminal.util.hashToMD5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NovelReadingViewModel @Inject constructor(
    private val repository: NovelRepository,
    private val chapterRepository: ChapterRepository,
    private val seriesRepository: SeriesRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    var state by mutableStateOf(
        NovelReadingState(
            currentChapter = savedStateHandle.toRoute<Screen.NovelReading>().currentChapter,
            detailPageUrl = savedStateHandle.toRoute<Screen.NovelReading>().detailPageUrl
        )
    )
        private set

    init {
        viewModelScope.launch {
            getSeriesEntity()
            getChapterContent(state.currentChapter.url)
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
                            Log.d(TAG, "getSeriesEntity: series: ${resource.data}")
                            state = state.copy(seriesEntity = resource.data)
                            getChapterList()
                        }
                    }
                }
            }
        }
    }

    private fun getChapterList() {
        viewModelScope.launch(Dispatchers.IO) {
            val id = state.seriesEntity?.id ?: return@launch
            chapterRepository.getChapters(id).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        Log.e(TAG, "getChapterList: hata: ${resource.exception}")
                    }

                    Resource.Loading -> {
                        Log.d(TAG, "getChapterList: loading")
                    }

                    is Resource.Success -> {
                        Log.d(TAG, "getChapterList: chapters : ${resource.data.size}")
                        state =
                            state.copy(chapters = resource.data.map { it.toChapter() }.reversed())
                    }
                }

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
        viewModelScope.launch(Dispatchers.IO) {
            repository.getNovelChapterContent(chapterUrl).collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            state = state.copy(
                                isLoading = false,
                                error = resource.exception.localizedMessage
                                    ?: "Bir hata oluştu lütfen tekrar deneyin"
                            )
                        }

                        Resource.Loading -> state = state.copy(isLoading = true, error = null)
                        is Resource.Success -> state =
                            state.copy(
                                isLoading = false,
                                error = null,
                                chapterContent = resource.data
                            )
                    }
                }


            }
        }
    }

    private fun getNextChapter() {
        setChapterIsReadTrue(state.currentChapter)
        val nextChapter = state.chapters.getNextChapter(state.currentChapter) ?: return
        updateSeriesCurrentChapterAndIndex(nextChapter)
        state = state.copy(currentChapter = nextChapter)
        getChapterContent(state.currentChapter.url)

    }

    private fun updateSeriesCurrentChapterAndIndex(chapter: Chapter) {
        viewModelScope.launch() {
            val chapterId = chapter.url.hashToMD5()
            val seriesEntity = state.seriesEntity?.copy(
                lastReadingDateTime = System.currentTimeMillis(),
                currentChapterId = chapterId,
                currentChapterName = chapter.title,
                currentPageIndex = 0
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

    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        updateSeriesCurrentChapterAndIndex(previousChapter)
        getChapterContent(state.currentChapter.url)
    }

    private fun setChapterIsReadTrue(chapter: Chapter) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = chapter.url.hashToMD5()
            chapterRepository.setIsReadByChapterId(id, isRead = true)
        }
    }

    companion object {
        private const val TAG = "NovelReadingViewModel"
    }
}


data class NovelReadingState(
    val chapters: List<Chapter> = emptyList(),
    val currentChapter: Chapter,
    val detailPageUrl: String,
    val chapterContent: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val seriesEntity: SeriesEntity? = null,
    val error: String? = null
)

sealed class NovelEvent {
    data object OnNextChapter : NovelEvent()
    data object OnPreviousChapter : NovelEvent()
    data object OnContentRetry : NovelEvent()
}