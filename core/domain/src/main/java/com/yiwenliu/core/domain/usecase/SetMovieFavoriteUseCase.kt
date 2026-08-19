package com.yiwenliu.core.domain.usecase

import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.common.result.EmptyResult
import com.yiwenliu.core.domain.repository.FavoriteMovieRepository
import com.yiwenliu.core.model.FavoriteMovie
import javax.inject.Inject

class SetMovieFavoriteUseCase
@Inject
constructor(private val favoriteMovieRepository: FavoriteMovieRepository) {
    suspend operator fun invoke(movie: FavoriteMovie, isFavorite: Boolean): EmptyResult<DataError.Local> =
        if (isFavorite) {
            favoriteMovieRepository.addFavorite(movie)
        } else {
            favoriteMovieRepository.removeFavorite(movie.id)
        }
}
