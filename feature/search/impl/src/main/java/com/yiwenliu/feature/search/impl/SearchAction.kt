package com.yiwenliu.feature.search.impl

internal sealed interface SearchAction {
    data class OnQueryStringChanged(val queryString: String) : SearchAction
}
