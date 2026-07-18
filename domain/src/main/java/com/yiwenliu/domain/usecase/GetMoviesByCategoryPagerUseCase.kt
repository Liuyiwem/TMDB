package com.yiwenliu.domain.usecase

import androidx.paging.PagingData
import com.yiwenliu.core.data.repository.MovieRepository
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.model.MovieCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesByCategoryPagerUseCase @Inject constructor(
    private val movieRepository: MovieRepository,
) {
    operator fun invoke(category: MovieCategory): Flow<PagingData<Movie>> =
        movieRepository.getMoviesByCategoryPager(category)
}
