plugins {
    alias(libs.plugins.tmdb.android.feature.impl)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.feature.home.impl"
}

dependencies {
    implementation(projects.feature.home.api)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    debugImplementation(libs.androidx.ui.test.manifest)
}
