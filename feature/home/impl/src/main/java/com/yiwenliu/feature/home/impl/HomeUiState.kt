package com.yiwenliu.feature.home.impl

import com.yiwenliu.core.model.MovieCategory

data class HomeUiState(
    val selectedCategory: MovieCategory = MovieCategory.NOW_PLAYING,
)
