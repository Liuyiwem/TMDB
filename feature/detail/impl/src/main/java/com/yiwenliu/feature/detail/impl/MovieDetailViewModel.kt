package com.yiwenliu.feature.detail.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.domain.usecase.GetMovieDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieDetailViewModel.Factory::class)
class MovieDetailViewModel
@AssistedInject
constructor(
    @Assisted private val movieId: Int,
    private val getMovieDetail: GetMovieDetailUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieDetailUiState())
    val state: StateFlow<MovieDetailUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onAction(action: MovieDetailAction) {
        when (action) {
            MovieDetailAction.OnRetry -> load()
            is MovieDetailAction.OnFavoriteToggle -> _state.update { it.copy(isFavorite = action.isFavorite) }
            is MovieDetailAction.OnRecommendationClick -> Unit
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getMovieDetail(movieId)) {
                is Result.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        detail = result.data.detail,
                        cast = result.data.cast,
                        recommendations = result.data.recommendations,
                        error = null,
                    )
                }

                is Result.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        detail = null,
                        cast = emptyList(),
                        recommendations = emptyList(),
                        error = result.error,
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailViewModel
    }
}
