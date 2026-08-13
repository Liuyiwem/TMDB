package com.yiwenliu.core.domain.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>>

    fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>>

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, DataError.Remote>

    suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, DataError.Remote>

    suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, DataError.Remote>
}
