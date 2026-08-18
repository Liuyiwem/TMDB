plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.room)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.database"
}

dependencies {
    api(libs.coroutines)
    api(libs.paging.runtime)

    implementation(libs.androidx.room.paging)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.test.coroutines)
}
