package com.yiwenliu.feature.home.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yiwenliu.core.model.Movie
import com.yiwenliu.domain.usecase.GetMoviesByCategoryPagerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val getMoviesByCategoryPager: GetMoviesByCategoryPagerUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val moviesPager: Flow<PagingData<Movie>> =
        _state
            .map { it.selectedCategory }
            .distinctUntilChanged()
            .flatMapLatest { getMoviesByCategoryPager(it) }
            .cachedIn(viewModelScope)

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnCategorySelected ->
                _state.update { it.copy(selectedCategory = action.category) }
        }
    }
}
