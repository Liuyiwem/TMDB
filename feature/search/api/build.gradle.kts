plugins {
    alias(libs.plugins.tmdb.android.feature.api)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.feature.search.api"
}
