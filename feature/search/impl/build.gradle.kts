plugins {
    alias(libs.plugins.tmdb.android.feature.impl)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.feature.search.impl"
}

dependencies {
    implementation(projects.feature.search.api)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    debugImplementation(libs.androidx.ui.test.manifest)
}
