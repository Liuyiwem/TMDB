import java.util.Properties
import kotlin.properties.Delegates

plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.ktlint)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yiwenliu.core.network"

    buildTypes {
        var baseUrl by Delegates.notNull<String>()
        var apiKey by Delegates.notNull<String>()
        var apiToken by Delegates.notNull<String>()
        Properties().apply {
            load(project.rootProject.file("local.properties").inputStream())
            baseUrl = getProperty("BASE_URL")
            apiKey = getProperty("API_KEY")
            apiToken = getProperty("API_TOKEN")
        }
        debug {
            buildConfigField("String", "BASE_URL", baseUrl)
            buildConfigField("String", "API_KEY", apiKey)
            buildConfigField("String", "API_TOKEN", apiToken)
        }
        release {
            buildConfigField("String", "BASE_URL", baseUrl)
            buildConfigField("String", "API_KEY", apiKey)
            buildConfigField("String", "API_TOKEN", apiToken)
        }
    }

    buildFeatures {
        buildConfig = true
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
