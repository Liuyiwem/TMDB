plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.android.buildConfig.secrets)
}

android {
    namespace = "com.yiwenliu.core.data"

    buildConfigSecrets {
        keys = listOf("IMAGE_URL")
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    api(libs.coroutines)
    api(libs.paging.runtime)
    api(projects.core.common)
    api(projects.core.model)

    implementation(projects.core.network)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.core.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
