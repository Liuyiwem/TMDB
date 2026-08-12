package com.yiwenliu.core.common.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

enum class TmdbDispatchers {
    Default,
    IO,
}

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val tmdbDispatcher: TmdbDispatchers)
