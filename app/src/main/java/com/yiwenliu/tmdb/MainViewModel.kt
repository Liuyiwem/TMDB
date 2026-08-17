package com.yiwenliu.tmdb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import com.yiwenliu.tmdb.navigation.TmdbDeepLinks
import com.yiwenliu.tmdb.splash.SplashConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(splashConfig: SplashConfig) : ViewModel() {
    var isSplashVisible by mutableStateOf(splashConfig.isEnabled)
        private set

    var deepLinkStack by mutableStateOf<List<NavKey>>(emptyList())
        private set

    fun onSplashFinished() {
        isSplashVisible = false
    }

    fun onDeepLink(uri: String?) {
        TmdbDeepLinks.parse(uri).takeIf { it.isNotEmpty() }?.let { deepLinkStack = it }
    }

    fun onDeepLinkHandled() {
        deepLinkStack = emptyList()
    }
}
