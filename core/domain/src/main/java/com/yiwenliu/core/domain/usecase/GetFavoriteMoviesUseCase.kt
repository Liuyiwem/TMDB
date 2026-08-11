package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.data.repository.FavoriteMovieRepository
import com.yiwenliu.core.model.FavoriteMovie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteMoviesUseCase
@Inject
constructor(private val favoriteMovieRepository: FavoriteMovieRepository) {
    operator fun invoke(): Flow<List<FavoriteMovie>> = favoriteMovieRepository.getFavoriteMovies()
}
