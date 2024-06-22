package com.novacodestudios.liminal.prensentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.novacodestudios.liminal.DetailScreen
import com.novacodestudios.liminal.data.repository.MangaRepository
import com.novacodestudios.liminal.data.repository.NovelRepository
import com.novacodestudios.liminal.domain.model.SeriesDetail
import com.novacodestudios.liminal.domain.model.SeriesPreview
import com.novacodestudios.liminal.domain.model.SeriesType
import com.novacodestudios.liminal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mangaRepo: MangaRepository,
    private val novelRepo: NovelRepository,
) : ViewModel() {
    var state by mutableStateOf(
        DetailState(
            detailPageUrl = savedStateHandle.toRoute<DetailScreen>().detailPageUrl,
            type = savedStateHandle.toRoute<DetailScreen>().type
        )
    )
        private set

    init {
        getDetail()
    }

    private fun getDetail() {
        when (state.type) {
            SeriesType.MANGA -> getMangaDetail()
            SeriesType.NOVEL -> getNovelDetail()
        }
    }

    private fun getMangaDetail() {
        viewModelScope.launch {
            mangaRepo.getMangaDetail(state.detailPageUrl).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        state = state.copy(isLoading = false)
                    }

                    Resource.Loading -> state = state.copy(isLoading = true)
                    is Resource.Success -> {
                        state = state.copy(isLoading = false, detail = resource.data)
                    }
                }
            }
        }
    }

    private fun getNovelDetail() {
        viewModelScope.launch {
            novelRepo.getNovelDetail(state.detailPageUrl).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        state = state.copy(isLoading = false)
                    }

                    Resource.Loading -> state = state.copy(isLoading = true)
                    is Resource.Success -> {
                        state = state.copy(isLoading = false, detail = resource.data)
                    }
                }
            }
        }
    }


}

data class DetailState(
    val isLoading: Boolean = false,
    val detailPageUrl: String,
    val type: SeriesType,
    val detail: SeriesDetail? = null,
)

