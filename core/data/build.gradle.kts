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

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }
}

dependencies {
    api(libs.coroutines)
    api(libs.paging.runtime)
    api(projects.core.common)
    api(projects.core.model)

    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(libs.retrofit2)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.core.testing)
}
