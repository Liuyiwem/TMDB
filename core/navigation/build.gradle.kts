plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.library.compose)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.savedstate.compose)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)

    testImplementation(libs.junit)
}
