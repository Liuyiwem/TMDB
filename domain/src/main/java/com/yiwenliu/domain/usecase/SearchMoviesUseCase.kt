package com.yiwenliu.domain.usecase

import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.MoviePage
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 1,
    ): Result<MoviePage, NetworkError> =
        movieRepository.searchMovies(query, page)
}
