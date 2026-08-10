package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.MovieDetailBundle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetMovieDetailUseCase
@Inject
constructor(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(movieId: Int): Result<MovieDetailBundle, NetworkError> = coroutineScope {
        val detailDeferred = async { movieRepository.getMovieDetail(movieId) }
        val castDeferred = async { movieRepository.getMovieCredits(movieId) }
        val recommendationsDeferred = async { movieRepository.getMovieRecommendations(movieId) }

        val detail = detailDeferred.await()
        val cast = castDeferred.await()
        val recommendations = recommendationsDeferred.await()

        when {
            detail is Result.Error -> Result.Error(detail.error)

            cast is Result.Error -> Result.Error(cast.error)

            recommendations is Result.Error -> Result.Error(recommendations.error)

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
