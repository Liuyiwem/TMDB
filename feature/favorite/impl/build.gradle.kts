plugins {
    alias(libs.plugins.tmdb.android.feature.impl)
}

android {
    namespace = "com.yiwenliu.feature.favorite.impl"
}

dependencies {
    implementation(projects.feature.favorite.api)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    debugImplementation(libs.androidx.ui.test.manifest)
}
