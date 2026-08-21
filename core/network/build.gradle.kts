plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.android.buildConfig.secrets)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yiwenliu.core.network"

    buildConfigSecrets {
        keys = listOf("BASE_URL", "API_TOKEN")
    }
}

dependencies {
    implementation(projects.core.common)

    api(libs.kotlinx.serialization.json)
    api(libs.coroutines)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit2)
    implementation(libs.kotlinx.serialization.retrofit2)

    testImplementation(libs.test.coroutines)
    testImplementation(libs.junit)
}
