package com.yiwenliu.feature.search.impl

data class SearchUiState(
    val queryString: String = "",
    val isPending: Boolean = false,
) {
    val isIdle: Boolean get() = queryString.isBlank()
}
