package com.yiwenliu.tmdb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SplashViewModel : ViewModel() {
    var isFinished by mutableStateOf(false)
        private set

    fun onSplashFinished() {
        isFinished = true
    }
}
