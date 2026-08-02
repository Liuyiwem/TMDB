package com.yiwenliu.feature.search.impl

import androidx.lifecycle.SavedStateHandle
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import com.yiwenliu.domain.usecase.SearchMoviesPagerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val movieRepository = TestMovieRepository()

    private fun viewModel(savedState: Map<String, Any?> = emptyMap()) = SearchViewModel(
        savedStateHandle = SavedStateHandle(savedState),
        searchMoviesPagerUseCase = SearchMoviesPagerUseCase(movieRepository),
    )

    @Test
    fun `query is empty when nothing was saved`() {
        assertEquals("", viewModel().state.value.queryString)
    }

    @Test
    fun `query is restored from SavedStateHandle`() {
        val restored = viewModel(mapOf(SearchViewModel.QUERY_STRING to "batman"))

        assertEquals("batman", restored.state.value.queryString)
    }

    @Test
    fun `onAction writes through to SavedStateHandle`() = runTest(testDispatcher) {
        val handle = SavedStateHandle()
        val searchViewModel = SearchViewModel(handle, SearchMoviesPagerUseCase(movieRepository))

        searchViewModel.state.test {
            assertEquals("", awaitItem().queryString)
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            assertEquals("batman", awaitItem().queryString)
            assertEquals("batman", handle.get<String>(SearchViewModel.QUERY_STRING))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `blank query never reaches the repository`() = runTest(testDispatcher) {
        val searchViewModel = viewModel()
        assertTrue(searchViewModel.searchMoviePager.asSnapshot().isEmpty())
        assertTrue(movieRepository.requestedQueries.isEmpty())
    }

    @Test
    fun `non-blank query returns movies and forwards the query`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()
        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
        val movies = searchViewModel.searchMoviePager.asSnapshot()
        assertEquals(moviesTestData.size, movies.size)
        assertEquals(listOf("batman"), movieRepository.requestedQueries)
    }

    @Test
    fun `a value that debounces back to the current query does not rebuild the pager`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()
        searchViewModel.searchMoviePager.test {
            awaitItem()
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
            awaitItem()
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batmans"))
            advanceTimeBy(50)
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failing query still records the request`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        movieRepository.sendError(IOException("boom"))
        val searchViewModel = viewModel()
        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
        assertFailsWith<IOException> { searchViewModel.searchMoviePager.asSnapshot() }
        assertEquals(listOf("batman"), movieRepository.requestedQueries)
    }

    @Test
    fun `a burst of keystrokes collapses into a single pager`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()
        searchViewModel.searchMoviePager.test {
            awaitItem()
            val query = "batman"
            query.indices.forEach { index ->
                searchViewModel.onAction(SearchAction.OnQueryStringChanged(query.take(index + 1)))
                advanceTimeBy(50)
            }
            expectNoEvents()
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS)
            awaitItem()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing the query takes effect without waiting for the debounce`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()
        searchViewModel.searchMoviePager.test {
            awaitItem()
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
            awaitItem()
            searchViewModel.onAction(SearchAction.OnQueryStringChanged(""))
            advanceTimeBy(1)
            val clearedAt = testScheduler.currentTime
            awaitItem()
            assertEquals(clearedAt, testScheduler.currentTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isPending is true while the pager is behind the text field`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()
        searchViewModel.searchMoviePager.test {
            searchViewModel.state.test {
                assertFalse(awaitItem().isPending)
                searchViewModel.onAction(SearchAction.OnQueryStringChanged("b"))
                assertTrue(awaitItem().isPending)
                advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
                assertFalse(awaitItem().isPending)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isPending is false for a blank query`() = runTest(testDispatcher) {
        viewModel().state.test {
            assertFalse(awaitItem().isPending)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
