plugins {
    alias(libs.plugins.tmdb.android.feature)
}

android {
    namespace = "com.yiwenliu.feature.movie"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.ui)
    implementation(projects.domain)
    implementation(projects.feature.movie.api)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(projects.core.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
