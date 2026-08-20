package com.yiwenliu.feature.favorite.impl

import app.cash.turbine.test
import com.yiwenliu.core.common.result.DataError
import com.yiwenliu.core.domain.usecase.GetFavoriteMoviesUseCase
import com.yiwenliu.core.domain.usecase.SetMovieFavoriteUseCase
import com.yiwenliu.core.testing.data.favoriteMoviesTestData
import com.yiwenliu.core.testing.repository.TestFavoriteMovieRepository
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

class FavoriteViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val favoriteMovieRepository = TestFavoriteMovieRepository()

    private fun viewModel() = FavoriteViewModel(
        getFavoriteMovies = GetFavoriteMoviesUseCase(favoriteMovieRepository),
        setMovieFavorite = SetMovieFavoriteUseCase(favoriteMovieRepository),
    )

    @Test
    fun `initial state is loading`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel().state.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            assertTrue(initial.favorites.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stored favorites reach the state and clear loading`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        viewModel().state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(favoriteMoviesTestData, loaded.favorites)
            expectNoEvents()
        }
    }

    @Test
    fun `an empty repository ends in an empty non-loading state`() = runTest(mainDispatcherRule.dispatcher) {
        viewModel().state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertTrue(loaded.favorites.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `OnRemoveClick only opens the dialog`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onAction(FavoriteAction.OnRemoveClick(favoriteMoviesTestData.first()))
            val opened = awaitItem()
            assertEquals(favoriteMoviesTestData.first(), opened.pendingRemoval)
            assertEquals(favoriteMoviesTestData, opened.favorites)
            expectNoEvents()
        }
        assertTrue(favoriteMovieRepository.attemptedRemovals.isEmpty())
    }

    @Test
    fun `OnRemoveConfirm removes the movie and closes the dialog`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onAction(FavoriteAction.OnRemoveClick(favoriteMoviesTestData.first()))
            viewModel.onAction(FavoriteAction.OnRemoveConfirm)
            runCurrent()
            val removed = expectMostRecentItem()
            assertNull(removed.pendingRemoval)
            assertEquals(favoriteMoviesTestData.drop(1), removed.favorites)
        }
        assertEquals(listOf(533535), favoriteMovieRepository.attemptedRemovals)
    }

    @Test
    fun `OnRemoveDismiss keeps the movie`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            awaitItem()
            viewModel.onAction(FavoriteAction.OnRemoveClick(favoriteMoviesTestData.first()))
            awaitItem()
            viewModel.onAction(FavoriteAction.OnRemoveDismiss)
            val dismissed = awaitItem()
            assertNull(dismissed.pendingRemoval)
            assertEquals(favoriteMoviesTestData, dismissed.favorites)
        }
        assertTrue(favoriteMovieRepository.attemptedRemovals.isEmpty())
    }

    @Test
    fun `a failed removal emits an error event`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        favoriteMovieRepository.sendWriteError(DataError.Local.DISK_FULL)
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            viewModel.events.test {
                viewModel.onAction(FavoriteAction.OnRemoveClick(favoriteMoviesTestData.first()))
                viewModel.onAction(FavoriteAction.OnRemoveConfirm)
                runCurrent()
                assertEquals(
                    FavoriteEvent.ShowError(UiText.StringResource(com.yiwenliu.core.ui.R.string.error_disk_full)),
                    awaitItem(),
                )
            }
            assertEquals(favoriteMoviesTestData, loaded.favorites)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a read failure keeps the loaded favorites and surfaces an error`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        viewModel().state.test {
            awaitItem()
            awaitItem()
            favoriteMovieRepository.sendReadError(DataError.Local.DISK_FULL)
            val failed = awaitItem()
            assertEquals(favoriteMoviesTestData, failed.favorites)
            assertEquals(
                UiText.StringResource(com.yiwenliu.core.ui.R.string.error_disk_full),
                failed.error,
            )
        }
    }

    @Test
    fun `OnErrorDismiss clears the error and keeps the favorites`() = runTest(mainDispatcherRule.dispatcher) {
        favoriteMovieRepository.sendFavoriteMovies(favoriteMoviesTestData)
        val viewModel = viewModel()
        viewModel.state.test {
            awaitItem()
            awaitItem()
            favoriteMovieRepository.sendReadError(DataError.Local.DISK_FULL)
            awaitItem()
            viewModel.onAction(FavoriteAction.OnErrorDismiss)
            val dismissed = awaitItem()
            assertNull(dismissed.error)
            assertEquals(favoriteMoviesTestData, dismissed.favorites)
        }
    }
}
