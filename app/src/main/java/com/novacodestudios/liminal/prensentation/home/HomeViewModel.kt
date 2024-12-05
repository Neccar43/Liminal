package com.novacodestudios.liminal.prensentation.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novacodestudios.liminal.domain.model.SeriesSummary
import com.novacodestudios.liminal.domain.use_case.GetSeriesList
import com.novacodestudios.liminal.domain.util.onError
import com.novacodestudios.liminal.domain.util.onSuccess
import com.novacodestudios.liminal.prensentation.util.UiText
import com.novacodestudios.liminal.prensentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSeriesList: GetSeriesList
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    // TODO: Loading işlmlerini kayanağa göre yap
    init {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            getSeriesList().collect { result ->
                result.onSuccess {
                    state = state.copy(seriesList = state.seriesList + it, isLoading = false)
                }.onError {
                    state = state.copy(isLoading = false)
                    Log.e(TAG, "getSeriesList: onError $it")
                    _eventFlow.emit(UIEvent.Error(it.toUiText()))
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

    // TODO: Aramayı gelişmiş hale getir
    private fun search() {
        viewModelScope.launch {
            delay(300)
            val query = state.query?.trim()
            if (query.isNullOrEmpty()) {
                state = state.copy(searchSeries = emptyList())
                return@launch
            }

            val searchResult = withContext(Dispatchers.Default) {
                state.seriesList.filter { series ->
                    series.name.contains(query, ignoreCase = true)
                }
            }

            state = state.copy(searchSeries = searchResult)
        }
    }


    sealed interface UIEvent {
        data class Error(val error: UiText) : UIEvent
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }

}

data class HomeState(
    val isLoading: Boolean = false,
    val seriesList: List<SeriesSummary> = emptyList(),
    val query: String? = null,
    val searchSeries: List<SeriesSummary> = emptyList(),
)

sealed class HomeEvent {
    data class OnQueryChanged(val query: String) : HomeEvent()
}

