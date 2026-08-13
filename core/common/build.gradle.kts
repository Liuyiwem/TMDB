plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.core.common"

    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    api(libs.coroutines)

    implementation(libs.retrofit2)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.test.coroutines)

    androidTestImplementation(libs.androidx.junit)
}
