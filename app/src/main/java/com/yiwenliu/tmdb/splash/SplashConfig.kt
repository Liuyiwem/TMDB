package com.yiwenliu.tmdb.splash

import javax.inject.Inject

interface SplashConfig {
    val isEnabled: Boolean
}

internal class DefaultSplashConfig @Inject constructor() : SplashConfig {
    override val isEnabled: Boolean = true
}
