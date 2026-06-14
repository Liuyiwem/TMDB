import java.util.Properties
import kotlin.apply
import kotlin.properties.Delegates

plugins {
    alias(libs.plugins.tmdb.android.library)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.core.data"

    buildTypes {
        var imageUrl by Delegates.notNull<String>()
        Properties().apply {
            load(project.rootProject.file("local.properties").inputStream())
            imageUrl = getProperty("IMAGE_URL")
        }
        debug {
            buildConfigField("String", "IMAGE_URL", imageUrl)
        }
        release {
            buildConfigField("String", "IMAGE_URL", imageUrl)
        }
    }

    buildFeatures {
        buildConfig = true
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
