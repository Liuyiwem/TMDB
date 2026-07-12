plugins {
    alias(libs.plugins.tmdb.android.application)
    alias(libs.plugins.tmdb.android.application.compose)
    alias(libs.plugins.tmdb.android.application.flavors)
    alias(libs.plugins.tmdb.android.hilt)
    alias(libs.plugins.tmdb.ktlint)
}

android {
    namespace = "com.yiwenliu.tmdb"

    defaultConfig {
        applicationId = "com.yiwenliu.tmdb"
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
        testInstrumentationRunner = "com.yiwenliu.core.data.test.HiltTestRunner"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.network)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.feature.movie.impl)
    implementation(projects.feature.movie.api)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.ui.test.manifest)

    kspAndroidTest(libs.hilt.compiler)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.hilt.testing)
}
