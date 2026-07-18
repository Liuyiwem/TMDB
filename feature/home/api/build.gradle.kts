plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.feature.home.api"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
