package com.yiwenliu.feature.favorite.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiwenliu.core.common.domain.util.onFailure
import com.yiwenliu.core.domain.usecase.GetFavoriteMoviesUseCase
import com.yiwenliu.core.domain.usecase.SetMovieFavoriteUseCase
import com.yiwenliu.core.model.FavoriteMovie
import com.yiwenliu.core.ui.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavoriteViewModel
@Inject
constructor(
    getFavoriteMovies: GetFavoriteMoviesUseCase,
    private val setMovieFavorite: SetMovieFavoriteUseCase,
) : ViewModel() {
    private val pendingRemoval = MutableStateFlow<FavoriteMovie?>(null)

    private val _events = Channel<FavoriteEvent>()
    val events: Flow<FavoriteEvent> = _events.receiveAsFlow()

    val state: StateFlow<FavoriteUiState> =
        combine(getFavoriteMovies(), pendingRemoval) { favorites, movie ->
            FavoriteUiState(
                isLoading = false,
                favorites = favorites,
                pendingRemoval = movie,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FavoriteUiState(),
        )

    fun onAction(action: FavoriteAction) {
        when (action) {
            is FavoriteAction.OnRemoveClick -> pendingRemoval.value = action.movie
            FavoriteAction.OnRemoveConfirm -> confirmRemoval()
            FavoriteAction.OnRemoveDismiss -> pendingRemoval.value = null
            is FavoriteAction.OnMovieClick -> Unit
        }
    }

    private fun confirmRemoval() {
        val movie = pendingRemoval.value ?: return
        pendingRemoval.value = null
        viewModelScope.launch {
            setMovieFavorite(movie, false)
                .onFailure { error -> _events.send(FavoriteEvent.ShowError(error.toUiText())) }
        }
    }
}
