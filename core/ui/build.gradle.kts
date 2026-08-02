plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.library.compose)
}

android {
    namespace = "com.yiwenliu.core.ui"
}

dependencies {
    api(projects.core.model)

    // api 而非 implementation：LazyPagingItems<Movie> 出現在 MoviePagingGrid 的公開簽章上。
    api(libs.paging.compose)

    // NetworkException 的訊息映射（Throwable.toUserMessage）。
    implementation(projects.core.common)

    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.material3)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
