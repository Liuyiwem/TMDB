package com.yiwenliu.core.data.util

import com.yiwenliu.core.data.BuildConfig

internal const val POSTER_SIZE = "w500"

internal const val BACKDROP_SIZE = "w780"

internal const val PROFILE_SIZE = "w185"

internal fun String?.asImageUrl(size: String = POSTER_SIZE): String? =
    if (this.isNullOrBlank() || size.isBlank()) null else "${BuildConfig.IMAGE_URL}$size$this"

internal fun String.asImagePath(): String {
    val sizedPath = removePrefix(BuildConfig.IMAGE_URL)
    if (sizedPath.length == length) return this
    val separator = sizedPath.indexOf('/')
    return if (separator == -1) this else sizedPath.substring(separator)
}
