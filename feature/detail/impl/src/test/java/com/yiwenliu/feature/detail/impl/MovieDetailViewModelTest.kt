package com.yiwenliu.feature.detail.impl

import app.cash.turbine.test
import com.yiwenliu.core.common.domain.util.DataError
import com.yiwenliu.core.domain.usecase.GetMovieDetailUseCase
import com.yiwenliu.core.domain.usecase.SetMovieFavoriteUseCase
import com.yiwenliu.core.testing.data.castTestData
import com.yiwenliu.core.testing.data.favoriteMoviesTestData
import com.yiwenliu.core.testing.data.movieDetailTestData
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestFavoriteMovieRepository
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import com.yiwenliu.core.ui.util.UiText
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovieDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val movieRepository = TestMovieRepository()

    private val favoriteMovieRepository = TestFavoriteMovieRepository()

    private val noInternetMessage = UiText.StringResource(com.yiwenliu.core.ui.R.string.error_no_internet)

    private fun viewModel(movieId: Int = 533535) = MovieDetailViewModel(
        movieId = movieId,
        getMovieDetail = GetMovieDetailUseCase(movieRepository, favoriteMovieRepository),
        setMovieFavorite = SetMovieFavoriteUseCase(favoriteMovieRepository),
    )

    private fun seedSuccess() {
        movieRepository.sendMovieDetail(movieDetailTestData)
        movieRepository.sendCast(castTestData)
        movieRepository.sendRecommendations(moviesTestData)
    }

    @Test
    fun `initial state is loading`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        viewModel().state.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            assertNull(initial.detail)
            assertNull(initial.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a successful load emits the bundle and clears loading`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        viewModel().state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(movieDetailTestData, loaded.detail)
            assertEquals(castTestData, loaded.cast)
            assertEquals(moviesTestData, loaded.recommendations)
            assertNull(loaded.error)
            expectNoEvents()
        }
    }

    @Test
    fun `the assisted movieId reaches the repository`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        viewModel(movieId = 1022789)
        runCurrent()
        assertEquals(listOf(1022789), movieRepository.requestedDetailIds)
        assertEquals(listOf(1022789), movieRepository.requestedCreditsIds)
        assertEquals(listOf(1022789), movieRepository.requestedRecommendationIds)
    }

    @Test
    fun `a failed load surfaces the error and clears loading`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        movieRepository.sendDetailError(DataError.Remote.NO_INTERNET)
        viewModel().state.test {
            awaitItem()
            val failed = awaitItem()
            assertFalse(failed.isLoading)
            assertEquals(noInternetMessage, failed.error)
            assertNull(failed.detail)
            expectNoEvents()
        }
    }

    @Test
    fun `OnRetry reloads and clears the previous error`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        movieRepository.sendDetailError(DataError.Remote.NO_INTERNET)
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            assertEquals(noInternetMessage, awaitItem().error)

            movieRepository.sendDetailError(null)
            viewModel.onAction(MovieDetailAction.OnRetry)
            assertTrue(awaitItem().isLoading)
            val reloaded = awaitItem()
            assertNull(reloaded.error)
            assertEquals(movieDetailTestData, reloaded.detail)
        }
    }

    @Test
    fun `OnFavoriteToggle persists the movie without refetching`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            assertFalse(awaitItem().isFavorite)

            viewModel.onAction(MovieDetailAction.OnFavoriteToggle(true))
            assertTrue(awaitItem().isFavorite)
            expectNoEvents()
        }
        assertEquals(listOf(533535), favoriteMovieRepository.attemptedAdds.map { it.id })
        assertEquals(listOf(533535), movieRepository.requestedDetailIds)
    }

    @Test
    fun `an already favorited movie starts with isFavorite true`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)

        val viewModel = viewModel()
        runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `OnFavoriteToggle false removes the movie`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        val viewModel = viewModel()
        runCurrent()

        viewModel.onAction(MovieDetailAction.OnFavoriteToggle(false))
        runCurrent()
        assertEquals(listOf(533535), favoriteMovieRepository.attemptedRemovals)
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `a failed favorite write emits an error event`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        favoriteMovieRepository.sendWriteError(DataError.Local.DISK_FULL)
        val viewModel = viewModel()
        runCurrent()

        viewModel.events.test {
            viewModel.onAction(MovieDetailAction.OnFavoriteToggle(true))
            runCurrent()
            assertEquals(
                MovieDetailEvent.ShowError(UiText.StringResource(com.yiwenliu.core.ui.R.string.error_disk_full)),
                awaitItem(),
            )
        }
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `OnFavoriteToggle survives a reload`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        val viewModel = viewModel()
        runCurrent()
        viewModel.onAction(MovieDetailAction.OnFavoriteToggle(true))
        viewModel.onAction(MovieDetailAction.OnRetry)
        runCurrent()
        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `OnRecommendationClick is ignored by the ViewModel`() = runTest(mainDispatcherRule.dispatcher) {
        seedSuccess()
        val viewModel = viewModel()
        runCurrent()
        val before = viewModel.state.value

        viewModel.onAction(MovieDetailAction.OnRecommendationClick(1022789, "Inside Out 2"))
        runCurrent()
        assertEquals(before, viewModel.state.value)
        assertEquals(listOf(533535), movieRepository.requestedDetailIds)
    }
}
