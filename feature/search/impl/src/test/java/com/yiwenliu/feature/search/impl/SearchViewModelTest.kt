package com.yiwenliu.feature.search.impl

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.yiwenliu.core.model.Movie
import com.yiwenliu.core.testing.data.moviesTestData
import com.yiwenliu.core.testing.repository.TestMovieRepository
import com.yiwenliu.core.testing.util.MainDispatcherRule
import com.yiwenliu.domain.usecase.SearchMoviesPagerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    /**
     * 提成欄位是必要的，不是風格選擇。
     *
     * [MainDispatcherRule] 設定的 `Dispatchers.Main` 就是 `viewModelScope` 用的 dispatcher，
     * 而 `runTest` 若不指定會自己建一個排程器。兩個排程器就是兩個獨立的虛擬時鐘 ——
     * `advanceTimeBy` 推的是 `runTest` 那個，ViewModel 裡的 `debounce(300)` 永遠不會到期，
     * 測試會掛住或變成 flaky。把同一個 dispatcher 同時給 rule 與 `runTest`，兩邊才共用時鐘。
     */
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val movieRepository = TestMovieRepository()

    private fun viewModel(savedState: Map<String, Any?> = emptyMap()) = SearchViewModel(
        savedStateHandle = SavedStateHandle(savedState),
        searchMoviesPagerUseCase = SearchMoviesPagerUseCase(movieRepository),
    )

    // ---------- 狀態與還原 ----------

    @Test
    fun `query is empty when nothing was saved`() {
        assertEquals("", viewModel().state.value.queryString)
    }

    @Test
    fun `query is restored from SavedStateHandle`() {
        // process death 之後 Hilt 交回來的 handle 會帶著上次的值。
        val restored = viewModel(mapOf(SearchViewModel.QUERY_STRING to "batman"))

        assertEquals("batman", restored.state.value.queryString)
    }

    @Test
    fun `onAction writes through to SavedStateHandle`() = runTest(testDispatcher) {
        // handle 是唯一真實來源，state 只是它的投影。改寫前這裡是兩份狀態手動同步，
        // 任何一邊漏寫都不會有編譯錯誤。
        val handle = SavedStateHandle()
        val searchViewModel = SearchViewModel(handle, SearchMoviesPagerUseCase(movieRepository))

        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))

        // 寫入 handle 是同步的。
        assertEquals("batman", handle.get<String>(SearchViewModel.QUERY_STRING))

        // 但 state 是 combine(...).stateIn(...) 的產物，需要一次 dispatch 才會反映出來 ——
        // 所以這裡必須 runCurrent()。這代表 state.value 是最終一致而非同步更新的；
        // UI 走 collectAsStateWithLifecycle 本來就是非同步收集，所以看不出差別。
        runCurrent()
        assertEquals("batman", searchViewModel.state.value.queryString)
    }

    // ---------- pager 行為 ----------

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
    fun `repeating the same query does not refetch`() = runTest(testDispatcher) {
        // 釘住 distinctUntilChanged。拿掉它 requestedQueries 會變成兩筆。
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
        searchViewModel.searchMoviePager.asSnapshot()
        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
        searchViewModel.searchMoviePager.asSnapshot()

        assertEquals(listOf("batman"), movieRepository.requestedQueries)
    }

    // ---------- debounce ----------

    @Test
    fun `a burst of keystrokes collapses into a single request`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        // 逐字打完 "batman"，每個字之間只推進 50ms —— 刻意小於 SEARCH_DEBOUNCE_MILLIS，
        // 所以 debounce 的計時器每次都被重置，中間不會有任何 emission。
        val query = "batman"
        query.indices.forEach { index ->
            searchViewModel.onAction(SearchAction.OnQueryStringChanged(query.take(index + 1)))
            advanceTimeBy(50)
        }

        // asSnapshot 會 suspend，runTest 因此自動推進虛擬時間跨過剩下的 debounce 視窗。
        searchViewModel.searchMoviePager.asSnapshot()

        // 六次鍵擊 → 一次請求，而且是最後那個完整字串。
        assertEquals(listOf(query), movieRepository.requestedQueries)
    }

    @Test
    fun `clearing the query takes effect without waiting for the debounce`() = runTest(testDispatcher) {
        // 靠的是 debounce 的 selector 多載在回傳 0 時直接 emit 不等待。
        // 沒有這個快速路徑，欄位會先清空、結果卻還留 300ms，畫面自相矛盾。
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        // 收集【外層】的 PagingData 流。注意這不會觸發 PagingSource.load()，所以這個測試
        // 不能斷言 requestedQueries —— 那需要 asSnapshot()，而 asSnapshot() 會推進虛擬時間，
        // 正好破壞這裡要測的「不等 debounce」。這裡只數 PagingData 的 emission。
        val emissions = mutableListOf<PagingData<Movie>>()
        val pagerJob = launch { searchViewModel.searchMoviePager.collect { emissions += it } }
        runCurrent()

        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
        advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
        runCurrent()
        val emissionsBeforeClear = emissions.size

        // 清空後【只】推進 1ms —— 遠小於 debounce 視窗。
        searchViewModel.onAction(SearchAction.OnQueryStringChanged(""))
        advanceTimeBy(1)
        runCurrent()

        assertTrue(
            emissions.size > emissionsBeforeClear,
            "清空查詢應該立即切到空結果，不該等待 debounce 視窗",
        )

        pagerJob.cancel()
    }

    // ---------- isPending ----------

    @Test
    fun `isPending is true while the pager is behind the text field`() = runTest(testDispatcher) {
        // 「打第一個字先閃一下 No results found」在 ViewModel 層的回歸測試。
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        // servedQuery 只在 pager 被收集時前進（onEach 在 cachedIn 上游，而 cachedIn 是
        // SharingStarted.Lazily），所以必須在背景收集，否則驗不到。
        val pagerJob = launch { searchViewModel.searchMoviePager.collect {} }
        runCurrent()

        searchViewModel.onAction(SearchAction.OnQueryStringChanged("b"))
        runCurrent()

        // debounce 還沒到期：typed = "b"、served = "" → pending → 畫面顯示 loading
        assertTrue(searchViewModel.state.value.isPending)

        advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
        runCurrent()

        // debounce 到期、servedQuery 已前進 → 不再 pending
        assertFalse(searchViewModel.state.value.isPending)

        pagerJob.cancel()
    }

    @Test
    fun `isPending is false for a blank query`() {
        // 空白不算 pending，否則初次進畫面會顯示 loading 而不是留白。
        assertFalse(viewModel().state.value.isPending)
    }
}
