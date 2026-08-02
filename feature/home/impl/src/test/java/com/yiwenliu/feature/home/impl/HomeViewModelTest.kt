package com.yiwenliu.feature.home.impl

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.yiwenliu.core.model.MovieCategory
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import com.yiwenliu.domain.usecase.GetMoviesByCategoryPagerUseCase
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

            // 原本比對前後兩個 .value，中間有沒有多餘的 emission 完全看不出來。
            // 這一行是 Turbine 帶來的新保證：狀態只變一次，不會有中間態閃過去。
            expectNoEvents()
        }
    }

    @Test
    fun `re-selecting the active category does not refetch`() = runTest(mainDispatcherRule.dispatcher) {
        // 釘住的是「重複點同一個分頁不會重建 Pager」這個行為（重建的後果是 grid 跳回頂端、
        // 白閃一下、多一次網路請求）。
        //
        // 注意這條【不是】 moviesPager 上 distinctUntilChanged 的守衛：實測拿掉它，這個測試
        // 一樣綠。真正擋下來的是 MutableStateFlow 自己的 equality conflation —— HomeUiState
        // 目前只有 selectedCategory 一個欄位，copy 出來的新值與舊值相等，_state 根本不會第
        // 二次發射。distinctUntilChanged 是第二層保險，等 HomeUiState 多出第二個欄位（那時
        // 改別的欄位會讓 _state 發射、但分類沒變）才會變成承重的那一層。
        movieRepository.sendMovies(moviesTestData)

        viewModel.moviesPager.asSnapshot()
        viewModel.onAction(HomeAction.OnCategorySelected(MovieCategory.NOW_PLAYING))
        runCurrent()
        viewModel.moviesPager.asSnapshot()

        assertEquals(listOf(MovieCategory.NOW_PLAYING), movieRepository.requestedCategories)
    }

    @Test
    fun `switching category refetches with the new category`() = runTest(mainDispatcherRule.dispatcher) {
        // 順序也重要：上一個測試單獨看，可以被「永遠不重抓」的錯誤實作騙過去。
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
        // asSnapshot() 遇到 LoadState.Error 會把 throwable 重新拋出，見
        // SearchMoviesPagerUseCaseTest 同名測試的說明。
        movieRepository.sendMovies(moviesTestData)
        movieRepository.sendError(IOException("boom"))

        assertFailsWith<IOException> { viewModel.moviesPager.asSnapshot() }
    }
}
