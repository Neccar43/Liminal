package com.novacodestudios.liminal.prensentation.novelReading

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.novacodestudios.liminal.NovelReadingScreen
import com.novacodestudios.liminal.data.repository.NovelRepository
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.prensentation.mangaReading.MangaEvent
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.getNextChapter
import com.novacodestudios.liminal.util.getPreviousChapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NovelReadingViewModel @Inject constructor(
    private val repository: NovelRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    var state by mutableStateOf(
        NovelReadingState(
            currentChapter = savedStateHandle.toRoute<NovelReadingScreen>().currentChapter,
            chapters = savedStateHandle.toRoute<NovelReadingScreen>().chapters
        )
    )
        private set

    init {
        getChapterContent(state.currentChapter.url)
    }

    fun onEvent(event: NovelEvent){
        when (event) {
            NovelEvent.OnNextChapter -> getNextChapter()
            NovelEvent.OnPreviousChapter -> getPreviousChapter()
        }
    }

    private fun getChapterContent(chapterUrl:String) {
        viewModelScope.launch {
            repository.getNovelChapterContent(chapterUrl).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> state = state.copy(
                        isLoading = false,
                        error = resource.exception.localizedMessage ?: "error"
                    )

                    Resource.Loading -> state = state.copy(isLoading = true)
                    is Resource.Success -> state =
                        state.copy(isLoading = false, chapterContent = resource.data)
                }

            }
        }
    }

    private fun getNextChapter() {
        val nextChapter = state.chapters.getNextChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = nextChapter)
        getChapterContent(state.currentChapter.url)

    }

    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        getChapterContent(state.currentChapter.url)
    }

    companion object {
        private const val TAG = "NovelReadingViewModel"
    }
}


data class NovelReadingState(
    val chapters: List<Chapter>,
    val currentChapter: Chapter,
    val chapterContent: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)

sealed class NovelEvent{
    data object OnNextChapter : NovelEvent()
    data object OnPreviousChapter : NovelEvent()
}