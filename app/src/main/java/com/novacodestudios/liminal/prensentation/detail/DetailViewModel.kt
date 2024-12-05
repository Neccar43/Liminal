package com.novacodestudios.liminal.prensentation.detail

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
import com.novacodestudios.liminal.domain.model.Series
import com.novacodestudios.liminal.domain.util.onError
import com.novacodestudios.liminal.domain.util.onSuccess
import com.novacodestudios.liminal.prensentation.navigation.Screen
import com.novacodestudios.liminal.prensentation.util.UiText
import com.novacodestudios.liminal.prensentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val seriesRepository: SeriesRepository,
    private val chapterRepository: ChapterRepository,
) : ViewModel() {
    var state by mutableStateOf(
        DetailState(
            detailPageUrl = savedStateHandle.toRoute<Screen.Detail>().detailPageUrl,
        )
    )
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getDetail()
        getChapters()
    }

    private fun getDetail() {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            seriesRepository.getSeriesDetail(state.detailPageUrl)
                .onSuccess {
                    state = state.copy(series = it, isLoading = false)
                }.onError {
                    state = state.copy(isLoading = false)
                    _eventFlow.emit(UIEvent.Error(it.toUiText()))
                }
        }
    }

    private fun getChapters() {
        state = state.copy(isChaptersLoading = true)
        viewModelScope.launch {
            seriesRepository.getSeriesChapterList(state.detailPageUrl)
                .onSuccess {
                    state = state.copy(
                        chapterList = it,
                        isChaptersLoading = false,
                        chapterListError = false
                    )
                }.onError {
                    state = state.copy(isChaptersLoading = false, chapterListError = true)
                    _eventFlow.emit(UIEvent.Error(it.toUiText()))
                }
        }
    }


    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.OnSeriesChapterClick -> saveSeriesAndChapters(event.chapter)

            DetailEvent.OnChapterListLoadRetry -> getChapters()
        }
    }

    private fun saveSeriesAndChapters(chapter: Chapter) {
        viewModelScope.launch {
            val series = state.series?.copy(chapters = state.chapterList) ?: run {
                _eventFlow.emit(UIEvent.Error(UiText.DynamicString("seri verisi alınamadı"))) // TODO: resource a kaydet
                return@launch
            }

            seriesRepository.upsert(series, currentChapter = chapter)
                .onError {
                    Log.e(TAG, "saveSeriesAndChapters: upsert: $it")
                    _eventFlow.emit(UIEvent.Error(it.toUiText()))
                }
            chapterRepository.insertAllChapters(state.chapterList, series = series)
                .onSuccess {
                    Log.d(TAG, "saveSeriesAndChapters: insertAll success")
                    _eventFlow.emit(UIEvent.NavigateReading(chapter.id))
                }
                .onError {
                    Log.e(TAG, "saveSeriesAndChapters: insertAll: $it")
                    _eventFlow.emit(UIEvent.Error(it.toUiText()))
                }


        }
    }


    companion object {
        private const val TAG = "DetailViewModel"
    }

    sealed interface UIEvent {
        data class Error(val error: UiText) : UIEvent
        data class NavigateReading(val chapterId: String) : UIEvent
    }

}

data class DetailState(
    val isLoading: Boolean = true,
    val detailPageUrl: String,
    val series: Series? = null,
    val chapterList: List<Chapter> = emptyList(),
    val isChaptersLoading: Boolean = false,
    val chapterListError: Boolean = false // TODO: Daha iyi bir mekanizma ile yap
)

sealed class DetailEvent {
    data class OnSeriesChapterClick(val chapter: Chapter) : DetailEvent()
    data object OnChapterListLoadRetry : DetailEvent()
}

