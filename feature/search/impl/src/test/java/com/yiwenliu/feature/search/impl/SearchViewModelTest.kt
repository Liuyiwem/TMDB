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
        // 這兩個還原測試刻意【不】訂閱：要驗的就是 stateIn 的 initialValue 有正確地從
        // SavedStateHandle 種進去。其餘要斷言 state【變動】的測試都必須用 Turbine 訂閱。
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

        // state 是 WhileSubscribed(5_000)：沒有訂閱者時 stateIn 不啟動上游，state.value
        // 會永遠停在 initialValue。Turbine 的 test { } 建立訂閱，上游才會跑，
        // awaitItem() 也直接表達「我在等下一個 emission」。
        searchViewModel.state.test {
            assertEquals("", awaitItem().queryString)

            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))

            assertEquals("batman", awaitItem().queryString)
            // 寫入 handle 本身是同步的。
            assertEquals("batman", handle.get<String>(SearchViewModel.QUERY_STRING))
            cancelAndIgnoreRemainingEvents()
        }
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
    fun `a value that debounces back to the current query does not rebuild the pager`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        searchViewModel.searchMoviePager.test {
            awaitItem() // 空查詢

            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
            awaitItem() // batman 的 Pager

            // 在同一個 debounce 視窗內多打一個字又刪掉，最後停回原本的 "batman"。
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batmans"))
            advanceTimeBy(50)
            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)

            // 不該有新的 Pager：debounce 只吐出穩定值 "batman"，
            // distinctUntilChanged 認出它就是上次放行的值。重建 Pager 會讓 grid
            // 跳回頂端並重抓一次。
            //
            // 實測過的變異：拿掉 distinctUntilChanged，或把 debounce 改成 0L，
            // 這個測試都會紅。
            //
            // 但【運算子順序】不是這個測試守的——把 distinctUntilChanged 排到
            // debounce 之前會直接【編譯失敗】，因為那樣它就作用在 StateFlow 上，
            // 而 kotlinx.coroutines 把 StateFlow.distinctUntilChanged() 標成
            // deprecated（"has no effect"，Operator Fusion），本專案又是
            // warnings-as-errors。編譯器擋得比測試早，也比測試牢。
            //
            // 不要改回「對 SavedStateHandle 寫入兩次相同的值」：StateFlow 會等值合併，
            // 第二次根本不發射，那樣連 distinctUntilChanged 都碰不到。
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failing query still records the request`() = runTest(testDispatcher) {
        // asSnapshot() 遇到 LoadState.Error 會把 throwable 重新拋出，見
        // SearchMoviesPagerUseCaseTest 同名測試的說明。這裡的重點是失敗【不會】讓查詢
        // 消失無蹤 —— 請求確實送出去過，UI 才有東西可以重試。
        movieRepository.sendMovies(moviesTestData)
        movieRepository.sendError(IOException("boom"))
        val searchViewModel = viewModel()

        searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))

        assertFailsWith<IOException> { searchViewModel.searchMoviePager.asSnapshot() }
        assertEquals(listOf("batman"), movieRepository.requestedQueries)
    }

    // ---------- debounce ----------

    @Test
    fun `a burst of keystrokes collapses into a single pager`() = runTest(testDispatcher) {
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        // 必須【先】訂閱。searchMoviePager 以 cachedIn 結尾，而 cachedIn 內部是
        // shareIn(started = SharingStarted.Lazily)——第一個訂閱者出現前，整條 debounce
        // 鏈一個運算子都不會執行。
        //
        // 先打字、後訂閱（例如先 onAction 六次再 asSnapshot）的話，typedQuery 這個
        // StateFlow 只會把【最後一個】值交出去，debounce 從頭到尾只看到一個值——
        // 那樣連把 debounce 整段刪掉都測不出來。
        searchViewModel.searchMoviePager.test {
            awaitItem() // 空查詢的初始分支（空白走 0ms 快速路徑，立刻到）

            // 逐字打完 "batman"，每個字之間只推進 50ms——刻意小於 SEARCH_DEBOUNCE_MILLIS，
            // 所以 debounce 的計時器每次都被重置。
            val query = "batman"
            query.indices.forEach { index ->
                searchViewModel.onAction(SearchAction.OnQueryStringChanged(query.take(index + 1)))
                advanceTimeBy(50)
            }

            // 打字期間一個 Pager 都不該送出來。expectNoEvents() 不推進時鐘，
            // 所以驗的是「到此刻（t=300）為止沒有任何 emission」——最後一次鍵擊在 t=250，
            // 它的 debounce 計時器排在 t=550。
            expectNoEvents()

            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS)
            awaitItem() // 六次鍵擊收斂成這一個
            expectNoEvents() // 而且只有這一個

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing the query takes effect without waiting for the debounce`() = runTest(testDispatcher) {
        // 靠的是 debounce 的 selector 多載在回傳 0 時直接 emit 不等待。
        // 沒有這個快速路徑，欄位會先清空、結果卻還留 300ms，畫面自相矛盾。
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()

        // 收集【外層】的 PagingData 流。注意這不會觸發 PagingSource.load()，所以這個測試
        // 不能斷言 requestedQueries —— 那需要 asSnapshot()，而 asSnapshot() 會推進虛擬時間，
        // 正好破壞這裡要測的「不等 debounce」。
        searchViewModel.searchMoviePager.test {
            awaitItem() // 初始的空白分支

            searchViewModel.onAction(SearchAction.OnQueryStringChanged("batman"))
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)
            awaitItem() // batman 的 Pager

            // 清空後【只】推進 1ms —— 遠小於 debounce 視窗。
            searchViewModel.onAction(SearchAction.OnQueryStringChanged(""))
            advanceTimeBy(1)

            val clearedAt = testScheduler.currentTime
            awaitItem()

            // 關鍵斷言：item 必須在【時鐘沒有前進】的情況下就已經到了。
            //
            // 不能只靠「awaitItem() 沒逾時」——runTest 會在測試協程 suspend 而當前虛擬
            // 時間無事可做時，自動把時鐘推進到下一個已排定的任務。所以就算 debounce 是
            // 300ms，awaitItem() 一樣拿得到 item，只是 currentTime 會多跳 299ms
            // （Turbine 預設 3 秒逾時在同一個虛擬時鐘下更晚，永遠輪不到它）。
            // 比較 currentTime 才是真正在驗 0ms 快速路徑。
            assertEquals(clearedAt, testScheduler.currentTime)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---------- isPending ----------

    @Test
    fun `isPending is true while the pager is behind the text field`() = runTest(testDispatcher) {
        // 「打第一個字先閃一下 No results found」在 ViewModel 層的回歸測試。
        movieRepository.sendMovies(moviesTestData)
        val searchViewModel = viewModel()
        // 使用 Turbine 同時測試 state 和 pager
        searchViewModel.searchMoviePager.test {
            searchViewModel.state.test {
                assertFalse(awaitItem().isPending) // 還沒輸入

                searchViewModel.onAction(SearchAction.OnQueryStringChanged("b"))

                // debounce 還沒到期：typed = "b"、served = "" → pending → 畫面顯示 loading
                assertTrue(awaitItem().isPending)

                advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MILLIS + 1)

                // debounce 到期、servedQuery 已前進 → 不再 pending
                assertFalse(awaitItem().isPending)
                cancelAndIgnoreRemainingEvents()
            }
            // 確保外層的 pager 測試在結束時忽略後續事件，避免殘留 job 導致測試掛起
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isPending is false for a blank query`() = runTest(testDispatcher) {
        // 空白不算 pending，否則初次進畫面會顯示 loading 而不是留白。
        // 一定要訂閱：不訂閱的話讀到的只是 initialValue（本來就是 false），等於沒測。
        viewModel().state.test {
            assertFalse(awaitItem().isPending)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
