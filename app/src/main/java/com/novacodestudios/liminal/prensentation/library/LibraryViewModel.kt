package com.novacodestudios.liminal.prensentation.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novacodestudios.liminal.data.locale.entity.SeriesEntity
import com.novacodestudios.liminal.data.repository.ChapterRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
            seriesRepository.getAllSeries().handleResource {
                state = state.copy(seriesEntityList = it)
            }
        }
    }

    sealed class UIState {
        data class ShowSnackBar(val message: String) : UIState()
    }

    private fun <T> Flow<Resource<T>>.handleResource(
        onSuccess: suspend (T) -> Unit
    ) {
        viewModelScope.launch {
            this@handleResource.collectLatest { resource ->
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

data class LibraryState(
    val isLoading: Boolean = false,
    val seriesEntityList: List<SeriesEntity> = emptyList(),
)

sealed class LibraryEvent {
    data class OnResetSeries(val seriesEntity: SeriesEntity) : LibraryEvent()
    data class OnDownloadSeries(val seriesEntity: SeriesEntity) : LibraryEvent()
}


