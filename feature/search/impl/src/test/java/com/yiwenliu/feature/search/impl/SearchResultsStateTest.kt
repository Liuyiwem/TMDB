package com.yiwenliu.feature.search.impl

import androidx.paging.LoadState
import org.junit.Test
import kotlin.test.assertEquals

class SearchResultsStateTest {
    /** 一個「已完成、沒有更多頁」的 refresh 狀態，多數案例的共同背景。 */
    private val settled = LoadState.NotLoading(endOfPaginationReached = true)

    @Test
    fun `blank query is Idle even when the pager still holds items`() {
        // 切換分頁回來或按 X 清空時會發生：pager 的快取還在，但畫面該回到留白。
        val state = searchResultsStateOf(
            isIdle = true,
            isPending = false,
            refresh = settled,
            itemCount = 20,
        )

        assertEquals(SearchResultsState.Idle, state)
    }

    @Test
    fun `pending query is Loading, not Empty`() {
        // 這是「打第一個字先閃一下 No results found」的回歸測試。
        // queryString 已非空白（isIdle = false），但 pager 還停在空白分支送出的那份 PagingData：
        // refresh = NotLoading、itemCount = 0。修正前這組輸入會掉進 itemCount == 0 → Empty。
        val state = searchResultsStateOf(
            isIdle = false,
            isPending = true,
            refresh = settled,
            itemCount = 0,
        )

        assertEquals(SearchResultsState.Loading, state)
    }

    @Test
    fun `pending query is Loading even when the pager reports the previous error`() {
        // 上一個查詢失敗，使用者改字重打。pager 還沒動，狀態仍是 Error ——
        // 這時該顯示 loading，不該把舊的錯誤留在畫面上。
        val state = searchResultsStateOf(
            isIdle = false,
            isPending = true,
            refresh = LoadState.Error(IllegalStateException("previous query failed")),
            itemCount = 0,
        )

        assertEquals(SearchResultsState.Loading, state)
    }

    @Test
    fun `refresh error is Error, not Empty`() {
        // 分支順序的回歸測試。refresh 失敗時 itemCount 必然是 0（每次改查詢都是全新的 Pager），
        // 所以若 itemCount == 0 的分支排在 Error 之前，網路失敗會被顯示成「找不到結果」——
        // 一個會讓人查很久的錯誤訊息。
        val cause = IllegalStateException("boom")

        val state = searchResultsStateOf(
            isIdle = false,
            isPending = false,
            refresh = LoadState.Error(cause),
            itemCount = 0,
        )

        assertEquals(SearchResultsState.Error(cause), state)
    }

    @Test
    fun `refresh loading is Loading`() {
        val state = searchResultsStateOf(
            isIdle = false,
            isPending = false,
            refresh = LoadState.Loading,
            itemCount = 0,
        )

        assertEquals(SearchResultsState.Loading, state)
    }

    @Test
    fun `settled query with no results is Empty`() {
        val state = searchResultsStateOf(
            isIdle = false,
            isPending = false,
            refresh = settled,
            itemCount = 0,
        )

        assertEquals(SearchResultsState.Empty, state)
    }

    @Test
    fun `settled query with results is Results`() {
        val state = searchResultsStateOf(
            isIdle = false,
            isPending = false,
            refresh = settled,
            itemCount = 6,
        )

        assertEquals(SearchResultsState.Results, state)
    }
}
