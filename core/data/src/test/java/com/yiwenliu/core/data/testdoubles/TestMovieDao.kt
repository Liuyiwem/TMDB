package com.yiwenliu.core.data.testdoubles

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yiwenliu.core.database.dao.MovieDao
import com.yiwenliu.core.database.model.MovieCategoryIndexEntity
import com.yiwenliu.core.database.model.MovieEntity
import com.yiwenliu.core.database.model.MovieRemoteKeyEntity

class TestMovieDao : MovieDao {
    private val movies = linkedMapOf<Int, MovieEntity>()
    private val categoryIndex = mutableListOf<MovieCategoryIndexEntity>()
    private val remoteKeys = mutableMapOf<String, MovieRemoteKeyEntity>()
    private val activeSources = mutableListOf<PagingSource<Int, MovieEntity>>()

    var errorToThrow: Throwable? = null

    val storedMovies: List<MovieEntity> get() = movies.values.toList()

    val storedIndex: List<MovieCategoryIndexEntity> get() = categoryIndex.toList()

    fun indexOf(category: String): List<MovieCategoryIndexEntity> =
        categoryIndex.filter { it.category == category }.sortedBy(MovieCategoryIndexEntity::position)

    fun moviesIn(category: String): List<MovieEntity> = indexOf(category).mapNotNull { movies[it.movieId] }

    fun seedRemoteKey(remoteKey: MovieRemoteKeyEntity) {
        remoteKeys[remoteKey.category] = remoteKey
    }

    override fun pagingSource(category: String): PagingSource<Int, MovieEntity> =
        object : PagingSource<Int, MovieEntity>() {
            override fun getRefreshKey(state: PagingState<Int, MovieEntity>): Int? = null

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieEntity> =
                LoadResult.Page(data = moviesIn(category), prevKey = null, nextKey = null)
        }.also { activeSources += it }

    private fun invalidateActiveSources() {
        val sources = activeSources.toList()
        activeSources.clear()
        sources.forEach(PagingSource<Int, MovieEntity>::invalidate)
    }

    override suspend fun remoteKey(category: String): MovieRemoteKeyEntity? = remoteKeys[category]

    override suspend fun maxPosition(category: String): Int =
        categoryIndex.filter { it.category == category }.maxOfOrNull(MovieCategoryIndexEntity::position) ?: -1

    override suspend fun upsertMovies(movies: List<MovieEntity>) {
        errorToThrow?.let { throw it }
        movies.forEach { this.movies[it.id] = it }
        invalidateActiveSources()
    }

    override suspend fun insertCategoryIndex(index: List<MovieCategoryIndexEntity>) {
        errorToThrow?.let { throw it }
        index.forEach { entry ->
            val exists = categoryIndex.any { it.category == entry.category && it.movieId == entry.movieId }
            if (!exists) categoryIndex += entry
        }
        invalidateActiveSources()
    }

    override suspend fun upsertRemoteKey(remoteKey: MovieRemoteKeyEntity) {
        errorToThrow?.let { throw it }
        remoteKeys[remoteKey.category] = remoteKey
    }

    override suspend fun clearCategoryIndex(category: String) {
        errorToThrow?.let { throw it }
        categoryIndex.removeAll { it.category == category }
        invalidateActiveSources()
    }

    override suspend fun deleteMoviesNotInAnyCategory() {
        errorToThrow?.let { throw it }
        val referenced = categoryIndex.map(MovieCategoryIndexEntity::movieId).toSet()
        movies.keys.retainAll(referenced)
        invalidateActiveSources()
    }
}
