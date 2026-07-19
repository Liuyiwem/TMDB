package com.yiwenliu.feature.movie.impl

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import com.yiwenliu.domain.usecase.GetPopularMoviesPagerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MovieViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val movieRepository = TestMovieRepository()

    private lateinit var viewModel: MovieViewModel

    @Before
    fun setup() {
        viewModel = MovieViewModel(GetPopularMoviesPagerUseCase(movieRepository))
    }

    @Test
    fun `popularMoviesPager emits movies from repository`() =
        runTest {
            movieRepository.sendPopularMovies(moviesTestData)

            val movies = viewModel.popularMoviesPager.asSnapshot()

            assertEquals(moviesTestData.size, movies.size)
            assertEquals(533535, movies[0].id)
            assertEquals("Deadpool & Wolverine", movies[0].title)
        }
}
