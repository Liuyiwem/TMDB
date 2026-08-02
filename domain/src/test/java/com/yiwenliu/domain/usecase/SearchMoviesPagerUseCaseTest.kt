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

        // 記錄發生在 PagingSource.load() 內，所以必須先 asSnapshot() 觸發載入。
        // 這個斷言擋的是「use case 把錯的東西傳下去」，例如順手 trim 掉了空白。
        assertEquals(listOf("star wars"), movieRepository.requestedQueries)
    }

    @Test
    fun `a repository failure reaches the collector as a load error`() = runTest {
        // 錯誤必須被【送達】而不是被吞掉。asSnapshot() 是測試 API，遇到 LoadState.Error
        // 會把 throwable 重新拋出 —— 所以這裡的 assertFailsWith 驗的是「錯誤有傳到底」，
        // 不是「正式環境會炸掉收集端」（正式環境是 UI 收到 LoadState.Error 去畫錯誤畫面）。
        movieRepository.sendMovies(moviesTestData)
        movieRepository.sendError(IOException("boom"))

        assertFailsWith<IOException> { useCase("batman").asSnapshot() }

        // 即使失敗，請求本身仍然發生過。
        assertEquals(listOf("batman"), movieRepository.requestedQueries)
    }
}
