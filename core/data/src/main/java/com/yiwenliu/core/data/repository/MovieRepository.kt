package com.yiwenliu.core.data.repository

import androidx.paging.PagingData
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>>

    fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>>
}
