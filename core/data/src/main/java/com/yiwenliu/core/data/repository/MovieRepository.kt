package com.yiwenliu.core.data.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MoviePage
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(page: Int = 1): Result<MoviePage, NetworkError>

    fun getPopularMoviesPager(): Flow<PagingData<Movie>>

    suspend fun getTopRatedMovies(page: Int = 1): Result<MoviePage, NetworkError>

    suspend fun getUpcomingMovies(page: Int = 1): Result<MoviePage, NetworkError>

    suspend fun searchMovies(
        query: String,
        page: Int = 1,
    ): Result<MoviePage, NetworkError>
}
