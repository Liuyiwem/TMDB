plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.data.test"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.network)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.test.runner)
    implementation(libs.hilt.testing)
}
