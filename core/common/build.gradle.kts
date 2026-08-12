plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.common"

    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    api(libs.retrofit2)
    api(libs.kotlinx.serialization.json)
    api(libs.coroutines)

    testImplementation(libs.junit)
    testImplementation(libs.test.coroutines)

    androidTestImplementation(libs.androidx.junit)
}
