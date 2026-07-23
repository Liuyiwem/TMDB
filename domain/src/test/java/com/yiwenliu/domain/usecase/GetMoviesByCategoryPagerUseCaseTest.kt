package com.yiwenliu.domain.usecase

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetMoviesByCategoryPagerUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val movieRepository = TestMovieRepository()
    private val useCase = GetMoviesByCategoryPagerUseCase(movieRepository)

    @Test
    fun `invoke delegates to repository and emits movies`() = runTest {
        movieRepository.sendMovies(moviesTestData)

        val movies = useCase(MovieCategory.POPULAR).asSnapshot()

        assertEquals(2, movies.size)
        assertEquals(533535, movies[0].id)
        assertEquals("Deadpool & Wolverine", movies[0].title)
    }
}
