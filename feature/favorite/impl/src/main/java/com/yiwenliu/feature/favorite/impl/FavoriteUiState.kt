package com.yiwenliu.feature.favorite.impl

import com.yiwenliu.core.model.FavoriteMovie
import com.yiwenliu.core.ui.util.UiText

internal data class FavoriteUiState(
    val isLoading: Boolean = true,
    val favorites: List<FavoriteMovie> = emptyList(),
    val pendingRemoval: FavoriteMovie? = null,
    val error: UiText? = null,
)
