package com.yiwenliu.tmdb

import com.android.build.api.dsl.Lint

internal fun configureLint(lint: Lint) = lint.apply {
    xmlReport = true
    sarifReport = true
    checkDependencies = true
    disable += "GradleDependency"
}
