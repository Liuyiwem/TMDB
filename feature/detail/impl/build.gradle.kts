plugins {
    alias(libs.plugins.tmdb.android.feature.impl)
}

android {
    namespace = "com.yiwenliu.feature.detail.impl"
}

dependencies {
    implementation(projects.feature.detail.api)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    debugImplementation(libs.androidx.ui.test.manifest)
}
