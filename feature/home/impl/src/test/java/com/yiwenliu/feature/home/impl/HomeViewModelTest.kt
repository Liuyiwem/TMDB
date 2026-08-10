package com.yiwenliu.feature.home.impl

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.yiwenliu.core.domain.usecase.GetMoviesByCategoryPagerUseCase
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val movieRepository = TestMovieRepository()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel(GetMoviesByCategoryPagerUseCase(movieRepository))
    }

    @Test
    fun `moviesPager emits movies from repository`() = runTest {
        movieRepository.sendMovies(moviesTestData)
        val movies = viewModel.moviesPager.asSnapshot()
        assertEquals(moviesTestData.size, movies.size)
        assertEquals(533535, movies[0].id)
        assertEquals("Deadpool & Wolverine", movies[0].title)
    }

    @Test
    fun `onAction OnCategorySelected updates state`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel.state.test {
            assertEquals(MovieCategory.NOW_PLAYING, awaitItem().selectedCategory)
            viewModel.onAction(HomeAction.OnCategorySelected(MovieCategory.TOP_RATED))
            assertEquals(MovieCategory.TOP_RATED, awaitItem().selectedCategory)
            expectNoEvents()
        }
    }

    @Test
    fun `re-selecting the active category does not refetch`() = runTest(mainDispatcherRule.dispatcher) {
        movieRepository.sendMovies(moviesTestData)
        viewModel.moviesPager.asSnapshot()
        viewModel.onAction(HomeAction.OnCategorySelected(MovieCategory.NOW_PLAYING))
        runCurrent()
        viewModel.moviesPager.asSnapshot()
        assertEquals(listOf(MovieCategory.NOW_PLAYING), movieRepository.requestedCategories)
    }

    @Test
    fun `switching category refetches with the new category`() = runTest(mainDispatcherRule.dispatcher) {
        movieRepository.sendMovies(moviesTestData)
        viewModel.moviesPager.asSnapshot()
        viewModel.onAction(HomeAction.OnCategorySelected(MovieCategory.TOP_RATED))
        runCurrent()
        viewModel.moviesPager.asSnapshot()
        assertEquals(
            listOf(MovieCategory.NOW_PLAYING, MovieCategory.TOP_RATED),
            movieRepository.requestedCategories,
        )
    }

    @Test
    fun `a repository failure reaches the collector as a load error`() = runTest(mainDispatcherRule.dispatcher) {
        movieRepository.sendMovies(moviesTestData)
        movieRepository.sendError(IOException("boom"))
        assertFailsWith<IOException> { viewModel.moviesPager.asSnapshot() }
    }
}
