package com.novacodestudios.liminal.prensentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.NovelRepository
import com.novacodestudios.liminal.domain.model.MangaPreview
import com.novacodestudios.liminal.domain.model.NovelPreview
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val novelRepo: NovelRepository,
    private val mangaRepo: MangaRepository
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    /*
    manga + novel 6656ms - 6600ms
    manga 1617ms 2601ms 1742ms
    novel 5558ms 2822ms 202ms

     */
    init {
        getContents()

    }

    private fun getContents() {
        getNovels()
        getMangas()

    }

    private fun getNovels() {
        viewModelScope.launch {
            novelRepo.getNovelList().collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        state = state.copy(isLoading = false)
                        Log.e(TAG, "getNovels: Hata ${resource.exception}")
                        _eventFlow.emit(
                            UIEvent.ShowToast(
                                resource.exception.localizedMessage ?: "An error occurred"
                            )
                        )
                        if (resource.exception is SocketTimeoutException) {
                            delay(1000L)
                            getNovels()
                            return@collectLatest
                        }

                    }

                    Resource.Loading -> state = state.copy(isLoading = true)
                    is Resource.Success -> {
                        state = state.copy(isLoading = false, novelList = resource.data)
                    }
                }
            }
        }
    }


    private fun getMangas() {
        viewModelScope.launch {
            mangaRepo.getMangas().collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        state = state.copy(isLoading = false)
                        Log.e(TAG, "getMangas: Hata: ${resource.exception}")
                        _eventFlow.emit(
                            UIEvent.ShowToast(
                                resource.exception.localizedMessage ?: "An error occurred"
                            )
                        )
                    }

                    Resource.Loading -> state = state.copy(isLoading = true)
                    is Resource.Success -> {
                        state = state.copy(isLoading = false, mangaList = resource.data)
                        Log.d(TAG, "getMangas: ${resource.data}")
                    }
                }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnQueryChanged -> {
                state = state.copy(query = event.query)
                search()
            }
        }
    }

    private fun search() {
        // TODO: Implement search
    }

    sealed class UIEvent {
        data class ShowToast(val message: String) : UIEvent()
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}

data class HomeState(
    val isLoading: Boolean = false,
    val error: String = "",
    val mangaList: List<MangaPreview> = emptyList(),
    val novelList: List<NovelPreview> = emptyList(),
    val query: String? = null,
    val searchSeries: List<SeriesPreview> = emptyList(),
) {
    val seriesList: List<SeriesPreview> = mangaList + novelList
}

sealed class HomeEvent {
    data class OnQueryChanged(val query: String) : HomeEvent()
}

