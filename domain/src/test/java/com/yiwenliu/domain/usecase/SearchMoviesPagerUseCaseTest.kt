package com.yiwenliu.domain.usecase

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class SearchMoviesPagerUseCaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val movieRepository = TestMovieRepository()

    private val useCase = SearchMoviesPagerUseCase(movieRepository)

    @Test
    fun `invoke emits the movies from the repository`() = runTest {
        movieRepository.sendMovies(moviesTestData)
        val movies = useCase("batman").asSnapshot()
        assertEquals(moviesTestData.size, movies.size)
        assertEquals(533535, movies.first().id)
    }

    @Test
    fun `invoke forwards the query string verbatim`() = runTest {
        movieRepository.sendMovies(moviesTestData)
        useCase("star wars").asSnapshot()
        assertEquals(listOf("star wars"), movieRepository.requestedQueries)
    }

    @Test
    fun `a repository failure reaches the collector as a load error`() = runTest {
        movieRepository.sendMovies(moviesTestData)
        movieRepository.sendError(IOException("boom"))
        assertFailsWith<IOException> { useCase("batman").asSnapshot() }
        assertEquals(listOf("batman"), movieRepository.requestedQueries)
    }
}
