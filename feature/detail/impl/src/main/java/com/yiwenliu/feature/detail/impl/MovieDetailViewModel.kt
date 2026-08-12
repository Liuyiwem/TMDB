package com.yiwenliu.feature.detail.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.domain.util.onFailure
import com.yiwenliu.core.domain.usecase.GetMovieDetailUseCase
import com.yiwenliu.core.domain.usecase.SetMovieFavoriteUseCase
import com.yiwenliu.core.model.asFavoriteMovie
import com.yiwenliu.core.ui.util.toUiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieDetailViewModel.Factory::class)
internal class MovieDetailViewModel
@AssistedInject
constructor(
    @Assisted private val movieId: Int,
    private val getMovieDetail: GetMovieDetailUseCase,
    private val setMovieFavorite: SetMovieFavoriteUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieDetailUiState())
    val state: StateFlow<MovieDetailUiState> = _state.asStateFlow()

    private val _events = Channel<MovieDetailEvent>()
    val events: Flow<MovieDetailEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onAction(action: MovieDetailAction) {
        when (action) {
            MovieDetailAction.OnRetry -> load()
            is MovieDetailAction.OnFavoriteToggle -> toggleFavorite(action.isFavorite)
            is MovieDetailAction.OnRecommendationClick -> Unit
        }
    }

    private fun toggleFavorite(isFavorite: Boolean) {
        val detail = _state.value.detail ?: return
        viewModelScope.launch {
            setMovieFavorite(detail.asFavoriteMovie(), isFavorite)
                .onFailure { error -> _events.send(MovieDetailEvent.ShowError(error.toUiText())) }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob = getMovieDetail(movieId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> _state.update {
                        it.copy(
                            isLoading = false,
                            detail = result.data.detail,
                            cast = result.data.cast,
                            recommendations = result.data.recommendations,
                            isFavorite = result.data.isFavorite,
                            error = null,
                        )
                    }

                    is Result.Failure -> _state.update {
                        it.copy(
                            isLoading = false,
                            detail = null,
                            cast = emptyList(),
                            recommendations = emptyList(),
                            error = result.error.toUiText(),
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailViewModel
    }
}
