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
import com.novacodestudios.liminal.domain.model.Chapter
import com.novacodestudios.liminal.domain.model.SeriesDetail
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.domain.model.toSeriesEntity
import com.novacodestudios.liminal.domain.model.toType
import com.novacodestudios.liminal.prensentation.screen.Screen
import com.novacodestudios.liminal.util.Resource
import com.novacodestudios.liminal.util.hashToMD5
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
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
            type = savedStateHandle.toRoute<Screen.Detail>().typeString.toType()
        )
    )
        private set

    private val _eventFlow = MutableSharedFlow<UIState>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            getContent()
        }
    }

    private fun getContent() {
        viewModelScope.launch(Dispatchers.IO) {
            getDetail()
        }
        viewModelScope.launch(Dispatchers.IO) {
            getChapterList()
        }
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.OnSeriesChapterClick -> {
                viewModelScope.launch {
                    launch(Dispatchers.IO) {
                        state =
                            state.copy(detail = state.detail?.copy2(chapters = state.chapterList))

                        val seriesEntity = state.detail!!.toSeriesEntity(state.detailPageUrl)
                            .copy(
                                currentChapterId = event.chapter.url.hashToMD5(),
                                currentChapterName = event.chapter.title
                            )

                        seriesRepository.upsert(seriesEntity).handleResource {
                            _eventFlow.emit(UIState.ShowSnackBar("kaydetme başarılı"))
                        }
                    }
                    launch(Dispatchers.IO) {
                        val chapterEntityList =
                            state.chapterList.toChapterEntityList(seriesId = state.detailPageUrl.hashToMD5())
                        chapterRepository.insertAllChapters(chapterEntityList).handleResource {
                            Log.d(
                                TAG,
                                "onEvent: chapterlar başarılı şekilde eklendi size ${chapterEntityList.size}"
                            )
                        }

                    }

                }

            }

            DetailEvent.OnChapterListLoadRetry -> getChapterList()
        }
    }

    private fun getDetail() {
        when (state.type) {
            SeriesType.MANGA -> getMangaDetail()
            SeriesType.NOVEL -> getNovelDetail()
        }
    }

    private fun getChapterList() {
        when (state.type) {
            SeriesType.MANGA -> getMangaChapterList()
            SeriesType.NOVEL -> getNovelChapterList()
        }
    }

    private fun getMangaChapterList() {
        viewModelScope.launch(Dispatchers.IO) {
            mangaRepo.getMangaChapterList(state.detailPageUrl).collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "getMangaChapterList: hata:${resource.exception}")
                            state = state.copy(
                                isChapterListLoading = false,
                                chapterListError = resource.exception.localizedMessage
                                    ?: "Bölümler yüklenrken bir hata oluştu lütfrn tekar deneyin"
                            )
                            _eventFlow.emit(UIState.ShowSnackBar(state.chapterListError!!))
                        }

                        Resource.Loading -> {
                            state = state.copy(isChapterListLoading = true, chapterListError = null)
                        }

                        is Resource.Success -> {
                            state = state.copy(
                                chapterListError = null,
                                isChapterListLoading = false,
                                chapterList = resource.data
                            )
                        }
                    }
                }

            }
        }
    }

    private fun getMangaDetail() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "getMangaDetail: manga detail çalıştı")
            mangaRepo.getMangaDetail(state.detailPageUrl).handleResource {
                state = state.copy(detail = it)
            }
        }
    }

    private fun getNovelDetail() {
        viewModelScope.launch(Dispatchers.IO) {
            novelRepo.getNovelDetail(state.detailPageUrl).collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "getNovelDetail: hata: ${resource.exception}")
                            state = state.copy(isLoading = false)
                            _eventFlow.emit(UIState.ShowSnackBar("Hata: ${resource.exception}"))

                            // TODO: Sınırla
                            if (resource.exception is SocketTimeoutException) {
                                delay(1000L)
                                getNovelDetail()
                                return@withContext
                            }
                        }

                        Resource.Loading -> {
                            state = state.copy(isLoading = true)
                        }

                        is Resource.Success -> {
                            state = state.copy(isLoading = false, detail = resource.data)
                        }
                    }
                }
            }
        }
    }

    private fun getNovelChapterList() {
        viewModelScope.launch(Dispatchers.IO) {
            novelRepo.getNovelChapters(state.detailPageUrl).collectLatest { resource ->
                withContext(Dispatchers.Main) {
                    when (resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "getNovelChapters: hata:${resource.exception}")
                            state = state.copy(
                                isChapterListLoading = false,
                                chapterListError = resource.exception.localizedMessage
                                    ?: "Bölümler yüklenrken bir hata oluştu lütfrn tekar deneyin"
                            )
                            _eventFlow.emit(UIState.ShowSnackBar(state.chapterListError!!))
                        }

                        Resource.Loading -> {
                            state = state.copy(isChapterListLoading = true, chapterListError = null)
                        }

                        is Resource.Success -> {
                            state = state.copy(
                                chapterListError = null,
                                isChapterListLoading = false,
                                chapterList = resource.data
                            )
                        }
                    }
                }

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
        viewModelScope.launch(Dispatchers.IO) {
            this@handleResource.collectLatest { resource ->
                withContext(Dispatchers.Main) {
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


}

data class DetailState(
    val isLoading: Boolean = true,
    val detailPageUrl: String,
    val type: SeriesType,
    val detail: SeriesDetail? = null,
    val chapterList: List<Chapter> = emptyList(),
    val isChapterListLoading: Boolean = false,
    val chapterListError: String? = null,
)

sealed class DetailEvent {
    data class OnSeriesChapterClick(val chapter: Chapter) : DetailEvent()
    data object OnChapterListLoadRetry : DetailEvent()
}

