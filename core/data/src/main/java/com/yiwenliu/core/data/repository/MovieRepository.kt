package com.yiwenliu.core.data.repository

import androidx.paging.PagingData
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.model.CastMember
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMoviesByCategoryPager(category: MovieCategory): Flow<PagingData<Movie>>

    fun searchMoviesPager(queryString: String): Flow<PagingData<Movie>>

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail, NetworkError>

    suspend fun getMovieCredits(movieId: Int): Result<List<CastMember>, NetworkError>

    suspend fun getMovieRecommendations(movieId: Int): Result<List<Movie>, NetworkError>
}
