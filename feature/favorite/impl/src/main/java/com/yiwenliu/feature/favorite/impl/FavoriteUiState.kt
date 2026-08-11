package com.yiwenliu.feature.favorite.impl

import com.yiwenliu.core.model.FavoriteMovie

internal data class FavoriteUiState(
    val isLoading: Boolean = true,
    val favorites: List<FavoriteMovie> = emptyList(),
    val pendingRemoval: FavoriteMovie? = null,
)
