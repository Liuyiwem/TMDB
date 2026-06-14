package com.yiwenliu.domain.usecase

import androidx.paging.PagingData
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPopularMoviesPagerUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
) {
    operator fun invoke(): Flow<PagingData<Movie>> =
        movieRepository.getPopularMoviesPager()
}
