package com.yiwenliu.tmdb

import org.junit.rules.ExternalResource

class DisableSplashRule : ExternalResource() {
    override fun before() {
        MainActivity.splashEnabled = false
    }

    override fun after() {
        MainActivity.splashEnabled = true
    }
}
