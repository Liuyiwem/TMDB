plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.domain"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(libs.coroutines)
    api(libs.paging.runtime)

    implementation(projects.core.data)

    testImplementation(projects.core.testing)
}
