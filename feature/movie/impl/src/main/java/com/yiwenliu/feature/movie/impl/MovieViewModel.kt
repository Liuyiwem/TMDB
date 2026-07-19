package com.yiwenliu.feature.movie.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yiwenliu.core.model.Movie
import com.yiwenliu.domain.usecase.GetPopularMoviesPagerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieViewModel
    @Inject
    constructor(
        getPopularMoviesPager: GetPopularMoviesPagerUseCase,
    ) : ViewModel() {
        val popularMoviesPager: Flow<PagingData<Movie>> =
            getPopularMoviesPager().cachedIn(viewModelScope)
    }
