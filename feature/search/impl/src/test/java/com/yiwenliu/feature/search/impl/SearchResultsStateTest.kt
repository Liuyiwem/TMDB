package com.yiwenliu.feature.search.impl

import androidx.paging.LoadState
import org.junit.Test
import kotlin.test.assertEquals

class SearchResultsStateTest {
    private val settled = LoadState.NotLoading(endOfPaginationReached = true)

    @Test
    fun `blank query is Idle even when the pager still holds items`() {
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
