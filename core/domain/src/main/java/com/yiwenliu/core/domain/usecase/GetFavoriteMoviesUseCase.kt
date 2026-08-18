package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.common.domain.util.Result
import com.yiwenliu.core.domain.repository.FavoriteMovieRepository
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteMoviesUseCase
@Inject
constructor(private val favoriteMovieRepository: FavoriteMovieRepository) {
    operator fun invoke(): Flow<Result<List<FavoriteMovie>, DataError.Local>> =
        favoriteMovieRepository.getFavoriteMovies()
}
