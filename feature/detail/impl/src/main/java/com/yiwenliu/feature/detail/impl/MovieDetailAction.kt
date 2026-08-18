package com.yiwenliu.feature.detail.impl

internal sealed interface MovieDetailAction {
    data class OnFavoriteToggle(val isFavorite: Boolean) : MovieDetailAction
}
