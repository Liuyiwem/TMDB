package com.yiwenliu.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.yiwenliu.core.database.model.MovieCategoryIndexEntity
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.database.model.MovieRemoteKeyEntity

@Dao
interface MovieDao {
    @Query(
        """
        SELECT m.* FROM movies m
        INNER JOIN movie_category_index i ON m.id = i.movieId
        WHERE i.category = :category
        ORDER BY i.position ASC
        """,
    )
    fun pagingSource(category: String): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movie_remote_keys WHERE category = :category")
    suspend fun remoteKey(category: String): MovieRemoteKeyEntity?

    @Query("SELECT COALESCE(MAX(position), -1) FROM movie_category_index WHERE category = :category")
    suspend fun maxPosition(category: String): Int

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoryIndex(index: List<MovieCategoryIndexEntity>)

    @Upsert
    suspend fun upsertRemoteKey(remoteKey: MovieRemoteKeyEntity)

    @Query("DELETE FROM movie_category_index WHERE category = :category")
    suspend fun clearCategoryIndex(category: String)

    @Query("DELETE FROM movies WHERE id NOT IN (SELECT movieId FROM movie_category_index)")
    suspend fun deleteMoviesNotInAnyCategory()

    @Transaction
    suspend fun saveCategoryPage(
        category: String,
        movies: List<MovieEntity>,
        remoteKey: MovieRemoteKeyEntity,
        clearExisting: Boolean,
    ) {
        if (clearExisting) {
            clearCategoryIndex(category)
            deleteMoviesNotInAnyCategory()
        }
        val offset = maxPosition(category) + 1
        upsertMovies(movies)
        insertCategoryIndex(
            movies.mapIndexed { indexInPage, movie ->
                MovieCategoryIndexEntity(
                    category = category,
                    movieId = movie.id,
                    position = offset + indexInPage,
                )
            },
        )
        upsertRemoteKey(remoteKey)
    }
}
