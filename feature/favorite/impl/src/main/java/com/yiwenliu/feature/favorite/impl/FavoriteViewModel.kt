package com.yiwenliu.feature.favorite.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.Result
import com.yiwenliu.core.common.result.onFailure
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class FavoritesResult(
    val favorites: List<FavoriteMovie> = emptyList(),
    val isLoading: Boolean = true,
    val error: DataError.Local? = null,
)

@HiltViewModel
internal class FavoriteViewModel
@Inject
constructor(
    getFavoriteMovies: GetFavoriteMoviesUseCase,
    private val setMovieFavorite: SetMovieFavoriteUseCase,
) : ViewModel() {
    private val pendingRemoval = MutableStateFlow<FavoriteMovie?>(null)

    private val errorDismissed = MutableStateFlow(false)

    private val _events = Channel<FavoriteEvent>()
    val events: Flow<FavoriteEvent> = _events.receiveAsFlow()

    private val favoritesResult: Flow<FavoritesResult> =
        getFavoriteMovies()
            .scan(FavoritesResult()) { previous, result ->
                when (result) {
                    is Result.Success -> FavoritesResult(favorites = result.data, isLoading = false)
                    is Result.Failure -> previous.copy(isLoading = false, error = result.error)
                }
            }
            .onStart { errorDismissed.value = false }

    val state: StateFlow<FavoriteUiState> =
        combine(favoritesResult, pendingRemoval, errorDismissed) { result, movie, dismissed ->
            FavoriteUiState(
                isLoading = result.isLoading,
                favorites = result.favorites,
                pendingRemoval = movie,
                error = result.error?.takeUnless { dismissed }?.toUiText(),
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
            FavoriteAction.OnErrorDismiss -> errorDismissed.value = true
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
