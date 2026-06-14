package com.yiwenliu.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

enum class TMDBDispatchers {
    Default,
    IO,
}

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(
    val tmdbDispatcher: TMDBDispatchers,
)
