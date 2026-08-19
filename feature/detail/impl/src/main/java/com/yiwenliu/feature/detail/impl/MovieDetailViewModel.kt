package com.yiwenliu.feature.detail.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.common.result.onFailure
import com.yiwenliu.core.domain.usecase.GetMovieDetailUseCase
import com.yiwenliu.core.domain.usecase.SetMovieFavoriteUseCase
import com.yiwenliu.core.model.asFavoriteMovie
import com.yiwenliu.core.ui.util.toUiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieDetailViewModel.Factory::class)
internal class MovieDetailViewModel
@AssistedInject
constructor(
    @Assisted private val movieId: Int,
    getMovieDetail: GetMovieDetailUseCase,
    private val setMovieFavorite: SetMovieFavoriteUseCase,
) : ViewModel() {
    private val _events = Channel<MovieDetailEvent>()
    val events: Flow<MovieDetailEvent> = _events.receiveAsFlow()

    val state: StateFlow<MovieDetailUiState> =
        getMovieDetail(movieId)
            .map { result ->
                when (result) {
                    is Result.Success -> MovieDetailUiState(
                        isLoading = false,
                        detail = result.data.detail,
                        cast = result.data.cast,
                        recommendations = result.data.recommendations,
                        isFavorite = result.data.isFavorite,
                    )

                    is Result.Failure -> MovieDetailUiState(
                        isLoading = false,
                        error = result.error.toUiText(),
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = MovieDetailUiState(),
            )

    fun onAction(action: MovieDetailAction) {
        when (action) {
            is MovieDetailAction.OnFavoriteToggle -> toggleFavorite(action.isFavorite)
        }
    }

    private fun toggleFavorite(isFavorite: Boolean) {
        val detail = state.value.detail ?: return
        viewModelScope.launch {
            setMovieFavorite(detail.asFavoriteMovie(), isFavorite)
                .onFailure { error -> _events.send(MovieDetailEvent.ShowError(error.toUiText())) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailViewModel
    }
}
