package com.yiwenliu.feature.search.impl

sealed interface SearchAction {
    data class OnQueryStringChanged(val queryString: String) : SearchAction
}
