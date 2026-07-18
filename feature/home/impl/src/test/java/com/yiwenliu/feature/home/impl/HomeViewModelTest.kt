package com.yiwenliu.feature.home.impl

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import com.yiwenliu.domain.usecase.GetMoviesByCategoryPagerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

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
    fun `moviesPager emits movies from repository`() =
        runTest {
            movieRepository.sendMovies(moviesTestData)

            val movies = viewModel.moviesPager.asSnapshot()

            assertEquals(moviesTestData.size, movies.size)
            assertEquals(533535, movies[0].id)
            assertEquals("Deadpool & Wolverine", movies[0].title)
        }

    @Test
    fun `onAction OnCategorySelected updates state`() =
        runTest {
            assertEquals(MovieCategory.NOW_PLAYING, viewModel.state.value.selectedCategory)

            viewModel.onAction(HomeAction.OnCategorySelected(MovieCategory.TOP_RATED))

            assertEquals(MovieCategory.TOP_RATED, viewModel.state.value.selectedCategory)
        }
}
