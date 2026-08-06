package com.yiwenliu.feature.detail.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.domain.usecase.GetMovieDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        load()
    }

    fun onAction(action: MovieDetailAction) {
        when (action) {
            MovieDetailAction.OnRetry -> load()
            is MovieDetailAction.OnFavoriteToggle -> _state.update { it.copy(isFavorite = action.isFavorite) }
            else -> Unit
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            _state.value = when (val result = getMovieDetail(movieId)) {
                is Result.Success -> MovieDetailUiState(
                    isLoading = false,
                    detail = result.data.detail,
                    cast = result.data.cast,
                    recommendations = result.data.recommendations,
                )

                is Result.Error -> MovieDetailUiState(isLoading = false, error = result.error)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailViewModel
    }
}
