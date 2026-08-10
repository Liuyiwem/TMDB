package com.yiwenliu.tmdb.splash

import javax.inject.Inject

internal class FakeSplashConfig @Inject constructor() : SplashConfig {
    override val isEnabled: Boolean = false
}
