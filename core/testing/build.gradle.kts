plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.testing"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)
    api(projects.core.domain)

    api(libs.test.coroutines)
    api(libs.test.turbine)
    api(libs.test.paging.common)
    api(libs.test.paging.testing)
    api(libs.hilt.testing)
    api(libs.junit)
}
