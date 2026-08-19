package com.yiwenliu.core.data.repository

import com.yiwenliu.core.model.MovieCategory

internal val MovieCategory.apiPath: String
    get() = when (this) {
        MovieCategory.NOW_PLAYING -> "now_playing"
        MovieCategory.POPULAR -> "popular"
        MovieCategory.TOP_RATED -> "top_rated"
        MovieCategory.UPCOMING -> "upcoming"
    }
