package com.yiwenliu.feature.search.impl

import androidx.paging.LoadState

internal sealed interface SearchResultsState {
    data object Idle : SearchResultsState

    data object Loading : SearchResultsState

    data class Error(val throwable: Throwable) : SearchResultsState

    data object Empty : SearchResultsState

    data object Results : SearchResultsState
}

internal fun searchResultsStateOf(
    isIdle: Boolean,
    isPending: Boolean,
    refresh: LoadState,
    itemCount: Int,
): SearchResultsState = when {
    isIdle -> SearchResultsState.Idle
    isPending || refresh is LoadState.Loading -> SearchResultsState.Loading
    refresh is LoadState.Error -> SearchResultsState.Error(refresh.error)
    itemCount == 0 -> SearchResultsState.Empty
    else -> SearchResultsState.Results
}
