package com.yiwenliu.feature.detail.impl

import androidx.compose.runtime.Immutable
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieDetail
import com.yiwenliu.core.ui.util.UiText

@Immutable
internal data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetail? = null,
    val cast: List<CastMember> = emptyList(),
    val recommendations: List<Movie> = emptyList(),
    val isFavorite: Boolean = false,
    val error: UiText? = null,
)
