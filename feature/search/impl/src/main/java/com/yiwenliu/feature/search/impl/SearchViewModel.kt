package com.yiwenliu.feature.search.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yiwenliu.core.model.Movie
import com.yiwenliu.domain.usecase.SearchMoviesPagerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchMoviesPagerUseCase: SearchMoviesPagerUseCase,
) : ViewModel() {
    /**
     * 使用者已輸入的字串。[SavedStateHandle] 是唯一真實來源，所以設定變更與 process death
     * 都能存活，也不需要手動同步第二份狀態。
     */
    private val typedQuery: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_STRING, "")

    /**
     * pager 實際正在服務的查詢，只在 debounce 到期後前進。
     *
     * 初值刻意是空字串，代表「還沒服務過任何查詢」——這樣還原一個非空查詢時，
     * 第一次組合就會是 pending，畫面直接顯示 loading。
     */
    private val servedQuery = MutableStateFlow("")

    val state: StateFlow<SearchUiState> =
        combine(typedQuery, servedQuery) { typed, served ->
            SearchUiState(
                queryString = typed,
                isPending = typed.isNotBlank() && typed != served,
            )
        }.stateIn(
            scope = viewModelScope,
            // WhileSubscribed：畫面離開超過 5 秒就停掉 combine，回來再續。
            //
            // 停止共享後 StateFlow 會【保留】最後一次發射的值（replayExpirationMillis
            // 預設是 Long.MAX_VALUE），所以 state.value 不會退回 initialValue ——
            // 只有從未有過任何訂閱者時才會是 initialValue。
            // 不要為了「修正」不存在的退回問題而加上 replayExpirationMillis：那才會製造
            // 真的 bug——背景超過 5 秒回來時搜尋框會閃一下建構當時的舊字串。
            //
            // 單元測試若要斷言 state 的【變動】，必須先建立訂閱（見 SearchViewModelTest
            // 用的 Turbine）。
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(typedQuery.value),
        )

    @OptIn(FlowPreview::class)
    val searchMoviePager: Flow<PagingData<Movie>> =
        typedQuery
            // 每個值用不同的 timeout：空白給 0，讓「按 X 清空」立即生效（debounceInternal 在
            // selector 回傳 0 時直接 emit 不等待）；打字給 300ms，把連續鍵擊收成一次請求。
            // 0L / 300L 的 L 後綴是必要的 —— 這個多載是 @OverloadResolutionByLambdaReturnType，
            // 回傳 Int 會解析不到。
            .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
            // debounce 必須在 distinctUntilChanged 之前。假設使用者停在 "a"，然後在視窗內
            // 打 b 又刪掉：debounce 只吐出穩定值 "a"，distinctUntilChanged 拿它跟上次【放行】
            // 的值比較後抑制掉，不會為一個結果已在畫面上的查詢重建 Pager。
            // 反過來排的話三個值都會放行，最後那個 "a" 會讓 grid 跳回頂端並重抓一次。
            //
            // 這個順序其實有編譯器把關：對調之後 distinctUntilChanged 會直接作用在
            // typedQuery 這個 StateFlow 上，而 kotlinx.coroutines 把
            // StateFlow.distinctUntilChanged() 標成 deprecated（"has no effect"，
            // 見 Operator Fusion），本專案是 warnings-as-errors，所以會編譯失敗。
            .distinctUntilChanged()
            .onEach { servedQuery.value = it }
            .flatMapLatest { query ->
                if (query.isBlank()) emptySearchResults() else searchMoviesPagerUseCase(query)
            }
            .cachedIn(viewModelScope)

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryStringChanged ->
                savedStateHandle[QUERY_STRING] = action.queryString
        }
    }

    companion object {
        internal const val QUERY_STRING = "queryString"

        internal const val SEARCH_DEBOUNCE_MILLIS = 300L
    }
}

/**
 * 空白查詢用的空結果。
 *
 * `sourceLoadStates` 是承重的參數，不是裝飾：無參數的 `PagingData.empty()` 送出
 * `sourceLoadStates = null`，`PagingDataPresenter` 因此不派發任何 load state，
 * 而 paging-compose 的初始值是 `LoadStates(refresh = LoadState.Loading, ...)`。
 * 把這個參數簡化掉，空白搜尋頁就會永久轉圈。
 */
private fun emptySearchResults(): Flow<PagingData<Movie>> = flowOf(
    PagingData.empty(
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)
