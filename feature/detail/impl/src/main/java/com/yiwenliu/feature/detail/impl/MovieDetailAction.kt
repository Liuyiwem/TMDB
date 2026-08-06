package com.yiwenliu.feature.detail.impl

sealed interface MovieDetailAction {
    data object OnRetry : MovieDetailAction

    data class OnRecommendationClick(
        val movieId: Int,
        val title: String,
    ) : MovieDetailAction

    data class OnFavoriteToggle(
        val isFavorite: Boolean,
    ) : MovieDetailAction
}
