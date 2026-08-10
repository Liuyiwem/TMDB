package com.yiwenliu.feature.search.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yiwenliu.core.domain.usecase.SearchMoviesPagerUseCase
import com.yiwenliu.core.model.Movie
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
    private val typedQuery: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_STRING, "")

    private val servedQuery = MutableStateFlow("")

    val state: StateFlow<SearchUiState> =
        combine(typedQuery, servedQuery) { typed, served ->
            SearchUiState(
                queryString = typed,
                isPending = typed.isNotBlank() && typed != served,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(typedQuery.value),
        )

    @OptIn(FlowPreview::class)
    val searchMoviePager: Flow<PagingData<Movie>> =
        typedQuery
            .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
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

private fun emptySearchResults(): Flow<PagingData<Movie>> = flowOf(
    PagingData.empty(
        sourceLoadStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = true),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        ),
    ),
)
