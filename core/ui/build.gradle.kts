plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.library.compose)
}

android {
    namespace = "com.yiwenliu.core.ui"
}

dependencies {
    api(projects.core.model)

    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
