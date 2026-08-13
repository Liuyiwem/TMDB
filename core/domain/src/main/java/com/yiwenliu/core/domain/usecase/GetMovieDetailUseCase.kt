package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.common.domain.util.map
import com.yiwenliu.core.domain.repository.FavoriteMovieRepository
import com.yiwenliu.core.domain.repository.MovieRepository
import com.yiwenliu.core.model.MovieDetailBundle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMovieDetailUseCase
@Inject
constructor(
    private val movieRepository: MovieRepository,
    private val favoriteMovieRepository: FavoriteMovieRepository,
) {
    operator fun invoke(movieId: Int): Flow<Result<MovieDetailBundle, DataError.Remote>> =
        flow { emit(fetchBundle(movieId)) }
            .combine(favoriteMovieRepository.isFavorite(movieId)) { bundle, isFavorite ->
                bundle.map { it.copy(isFavorite = isFavorite) }
            }

    private suspend fun fetchBundle(movieId: Int): Result<MovieDetailBundle, DataError.Remote> = coroutineScope {
        val detailDeferred = async { movieRepository.getMovieDetail(movieId) }
        val castDeferred = async { movieRepository.getMovieCredits(movieId) }
        val recommendationsDeferred = async { movieRepository.getMovieRecommendations(movieId) }

        val detail = detailDeferred.await()
        val cast = castDeferred.await()
        val recommendations = recommendationsDeferred.await()

        when {
            detail is Result.Failure -> Result.Failure(detail.error)

            cast is Result.Failure -> Result.Failure(cast.error)

            recommendations is Result.Failure -> Result.Failure(recommendations.error)

            else -> Result.Success(
                MovieDetailBundle(
                    detail = (detail as Result.Success).data,
                    cast = (cast as Result.Success).data,
                    recommendations = (recommendations as Result.Success).data,
                ),
            )
        }
    }
}
