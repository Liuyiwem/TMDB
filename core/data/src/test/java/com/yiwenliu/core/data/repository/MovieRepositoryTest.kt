package com.yiwenliu.core.data.repository

import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.common.domain.util.NetworkError
import com.yiwenliu.core.common.domain.util.NetworkException
import com.yiwenliu.core.data.testdoubles.TestTMDBApiService
import com.yiwenliu.core.model.MovieCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var apiService: TestTMDBApiService

    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setup() {
        apiService = TestTMDBApiService()
        repository = MovieRepositoryImpl(apiService, testDispatcher)
    }

    @Test
    fun `getMoviesByCategoryPager firstLoad returns CorrectMovies`() = runTest(testDispatcher) {
        val movies = repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()

        assertEquals(2, movies.size)
        assertEquals(533535, movies[0].id)
        assertEquals("Deadpool & Wolverine", movies[0].title)
    }

    @Test
    fun `searchMoviesPager firstLoad returns CorrectMovies`() = runTest(testDispatcher) {
        val movies = repository.searchMoviesPager("fight").asSnapshot()

        // 回傳的是搜尋 asset 的 Fight Club 而不是 popular asset 的內容，
        // 所以這一個斷言就同時證明了「合併後的兩個 pager 各自接到正確的 fetcher」。
        assertEquals(1, movies.size)
        assertEquals(550, movies[0].id)
        assertEquals("Fight Club", movies[0].title)
    }

    // ---------- 錯誤路徑 ----------
    //
    // 這一層要驗的是「錯誤有沒有穿過 Pager 抵達 Flow<PagingData>」，跟 MoviePagingSourceTest
    // 驗的「load() 回傳什麼」是不同的問題。
    //
    // 注意拋出的是 NetworkException 而不是原始的 IOException：safeCall 在 MoviePagingSource
    // 裡就把它映射掉了。use case 層那幾個同類測試看到的是 IOException，因為 TestMovieRepository
    // 直接回傳 LoadResult.Error(IOException)，根本沒有經過 safeCall。

    @Test
    fun `getMoviesByCategoryPager surfaces a failure as a load error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")

        val thrown = assertFailsWith<NetworkException> {
            repository.getMoviesByCategoryPager(MovieCategory.POPULAR).asSnapshot()
        }

        assertEquals(NetworkError.NO_INTERNET, thrown.networkError)
    }

    @Test
    fun `searchMoviesPager surfaces a failure as a load error`() = runTest(testDispatcher) {
        apiService.errorToThrow = IOException("boom")

        val thrown = assertFailsWith<NetworkException> {
            repository.searchMoviesPager("batman").asSnapshot()
        }

        assertEquals(NetworkError.NO_INTERNET, thrown.networkError)
    }
}
