package com.yiwenliu.core.domain.usecase

import androidx.paging.PagingData
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMoviesPagerUseCase
@Inject
constructor(
    private val movieRepository: MovieRepository,
) {
    operator fun invoke(queryString: String): Flow<PagingData<Movie>> = movieRepository.searchMoviesPager(queryString)
}
