plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.library.compose)
}

android {
    namespace = "com.yiwenliu.core.ui"
}

dependencies {
    api(projects.core.model)
    api(libs.paging.compose)

    implementation(projects.core.common)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)
}
