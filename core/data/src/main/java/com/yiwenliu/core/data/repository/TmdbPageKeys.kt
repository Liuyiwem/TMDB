package com.yiwenliu.core.data.repository

internal const val TMDB_MAX_PAGE = 500

internal fun nextPageKeyOf(page: Int, totalPages: Int): Int? =
    if (page >= minOf(totalPages, TMDB_MAX_PAGE)) null else page + 1

internal fun prevPageKeyOf(page: Int): Int? = if (page == 1) null else page - 1
