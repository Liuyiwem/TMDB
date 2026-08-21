plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.network.mock"

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(projects.core.network)
    implementation(projects.core.common)

    testImplementation(libs.junit)
    testImplementation(libs.test.coroutines)
}
