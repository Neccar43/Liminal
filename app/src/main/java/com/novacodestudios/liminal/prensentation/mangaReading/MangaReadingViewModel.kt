package com.novacodestudios.liminal.prensentation.mangaReading

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.novacodestudios.liminal.MangaReadingScreen
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.getNextChapter
import com.novacodestudios.liminal.util.getPreviousChapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MangaRepository
) : ViewModel() {
    var state by mutableStateOf(
        MangaState(
            currentChapter = savedStateHandle.toRoute<MangaReadingScreen>().currentChapter,
            chapters = savedStateHandle.toRoute<MangaReadingScreen>().chapters.reversed()
        )
    )
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getMangaImages(state.currentChapter!!.url)
    }


    private fun getMangaImages(chapterUrl: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            repository.getMangaImageUrls(chapterUrl).collectLatest { resource ->
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

    fun onEvent(event: MangaEvent) {
        when (event) {
            is MangaEvent.OnReadingModeChanged -> {
                state = state.copy(readingMode = event.readingMode)
            }

            MangaEvent.OnNextChapter -> getNextChapter()
            MangaEvent.OnPreviousChapter -> getPreviousChapter()
        }
    }

    private fun getNextChapter() {
        val nextChapter = state.chapters.getNextChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = nextChapter)
        getMangaImages(state.currentChapter.url)

    }

    private fun getPreviousChapter() {
        val previousChapter = state.chapters.getPreviousChapter(state.currentChapter) ?: return
        state = state.copy(currentChapter = previousChapter)
        getMangaImages(state.currentChapter.url)
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
    val readingMode: ReadingMode = ReadingMode.RIGHT_TO_LEFT,
)

enum class ReadingMode {
    WEBTOON,
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

sealed class MangaEvent {
    data class OnReadingModeChanged(val readingMode: ReadingMode) : MangaEvent()
    data object OnNextChapter : MangaEvent()
    data object OnPreviousChapter : MangaEvent()
}

