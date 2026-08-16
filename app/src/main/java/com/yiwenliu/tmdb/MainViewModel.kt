package com.yiwenliu.tmdb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.yiwenliu.tmdb.splash.SplashConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(splashConfig: SplashConfig) : ViewModel() {
    var isSplashVisible by mutableStateOf(splashConfig.isEnabled)
        private set

    fun onSplashFinished() {
        isSplashVisible = false
    }
}
