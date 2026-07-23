plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.domain"
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(libs.coroutines)
    api(libs.paging.runtime)

    implementation(projects.core.data)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.core.testing)
}
