package com.yiwenliu.feature.home.impl

import com.yiwenliu.core.model.MovieCategory

internal sealed interface HomeAction {
    data class OnCategorySelected(val category: MovieCategory) : HomeAction
}
