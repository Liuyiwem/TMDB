plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.core.testing"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    api(projects.core.data)

    api(libs.test.coroutines)
    api(libs.test.paging.common)
    api(libs.test.paging.testing)
    api(libs.hilt.testing)
    api(libs.junit)
}
