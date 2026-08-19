package com.yiwenliu.feature.favorite.impl

import androidx.compose.runtime.Immutable
import com.yiwenliu.core.model.FavoriteMovie
import com.yiwenliu.core.ui.util.UiText

@Immutable
internal data class FavoriteUiState(
    val isLoading: Boolean = true,
    val favorites: List<FavoriteMovie> = emptyList(),
    val pendingRemoval: FavoriteMovie? = null,
    val error: UiText? = null,
)
