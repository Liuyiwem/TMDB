plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.ktlint)
    alias(libs.plugins.tmdb.android.buildConfig.secrets)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yiwenliu.core.network"

    buildConfigSecrets {
        keys = listOf("BASE_URL", "API_TOKEN")
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit2)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.retrofit2)
    implementation(libs.coroutines)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.test.coroutines)
    testImplementation(libs.hilt.testing)
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.hilt.testing)
}
