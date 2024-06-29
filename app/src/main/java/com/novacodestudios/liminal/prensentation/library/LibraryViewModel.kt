package com.novacodestudios.liminal.prensentation.library

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.mapper.toChapter
import com.novacodestudios.liminal.domain.mapper.toChapterList
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val seriesRepository: SeriesRepository,
    private val chapterRepository: ChapterRepository,
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

    // TODO: Implement downloadSeries
    private fun downloadSeries(seriesEntity: SeriesEntity) {

    }

    private fun resetSeries(series: SeriesEntity) {
        viewModelScope.launch {
            seriesRepository.deleteSeries(series).handleResource {
                getSeriesList()
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


