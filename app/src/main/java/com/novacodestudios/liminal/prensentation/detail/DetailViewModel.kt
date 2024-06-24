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
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.NovelRepository
import com.novacodestudios.liminal.data.repository.SeriesRepository
import com.novacodestudios.liminal.domain.mapper.toChapterEntityList
import com.novacodestudios.liminal.domain.model.SeriesDetail
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.toSeriesEntity
import com.novacodestudios.liminal.prensentation.screen.Screen
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.hashToMD5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mangaRepo: MangaRepository,
    private val novelRepo: NovelRepository,
    private val seriesRepository: SeriesRepository,
    private val chapterRepository: ChapterRepository,
) : ViewModel() {
    var state by mutableStateOf(
        DetailState(
            detailPageUrl = savedStateHandle.toRoute<Screen.Detail>().detailPageUrl,
            type = savedStateHandle.toRoute<Screen.Detail>().type
        )
    )
        private set

    private val _eventFlow = MutableSharedFlow<UIState>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        getDetail()
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            DetailEvent.OnSeriesChapterClick -> {
                viewModelScope.launch {
                    launch {
                        val seriesEntity =state.detail!!.toSeriesEntity(state.detailPageUrl)
                        seriesRepository.insertSeries(seriesEntity).handleResource {
                            _eventFlow.emit(UIState.ShowSnackBar("kaydetme başarılı"))
                        }
                    }
                    launch {
                        val chapterEntityList=state.detail!!.chapters.toChapterEntityList(seriesId = state.detailPageUrl.hashToMD5())
                        chapterRepository.insertAllChapters(chapterEntityList).handleResource {

                        }
                    }

                }

            }
        }
    }

        private fun getDetail() {
            when (state.type) {
                SeriesType.MANGA -> getMangaDetail()
                SeriesType.NOVEL -> getNovelDetail()
            }
        }

        private fun getMangaDetail() {
            viewModelScope.launch {
                _eventFlow.emit(UIState.ShowSnackBar("kaydetme başarılı"))
                Log.d(TAG, "getMangaDetail: manga detail çalıştı")
                mangaRepo.getMangaDetail(state.detailPageUrl).handleResource {
                    state = state.copy(detail = it)
                }
            }
        }

        private fun getNovelDetail() {
            viewModelScope.launch {
                novelRepo.getNovelDetail(state.detailPageUrl).handleResource {
                    state = state.copy(detail = it)
                }
            }
        }

        companion object {
            private const val TAG = "DetailViewModel"
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
                            Log.e(TAG, "handleResource: error ${resource.exception}")
                            state = state.copy(isLoading = false)
                            _eventFlow.emit(
                                UIState.ShowSnackBar(
                                    resource.exception.localizedMessage ?: "An error occurred"
                                )
                            )
                        }

                        Resource.Loading -> {
                            Log.d(TAG, "handleResource: loading")
                            state = state.copy(isLoading = true)
                        }

                        is Resource.Success -> {
                            state = state.copy(isLoading = false)
                            Log.d(TAG, "handleResource: success")
                            onSuccess(resource.data)
                        }
                    }
                }
            }
        }


    }

    data class DetailState(
        val isLoading: Boolean = true,
        val detailPageUrl: String,
        val type: SeriesType,
        val detail: SeriesDetail? = null,
    )

    sealed class DetailEvent {
        data object OnSeriesChapterClick : DetailEvent()
    }

