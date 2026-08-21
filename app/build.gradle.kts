plugins {
    alias(libs.plugins.tmdb.android.application)
    alias(libs.plugins.tmdb.android.application.compose)
    alias(libs.plugins.tmdb.android.application.flavors)
    alias(libs.plugins.tmdb.android.hilt)
}

val releaseKeystorePath = providers.environmentVariable("KEYSTORE_PATH")

android {
    namespace = "com.yiwenliu.tmdb"

    defaultConfig {
        applicationId = "com.yiwenliu.tmdb"
        versionCode = providers.environmentVariable("VERSION_CODE").map(String::toInt).getOrElse(1)
        versionName = providers.environmentVariable("VERSION_NAME").getOrElse("1.0")
        testInstrumentationRunner = "com.yiwenliu.core.data.test.HiltTestRunner"
    }

    signingConfigs {
        if (releaseKeystorePath.isPresent) {
            create("release") {
                storeFile = file(releaseKeystorePath.get())
                storePassword = providers.environmentVariable("KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.home.api)
    implementation(projects.feature.search.impl)
    implementation(projects.feature.search.api)
    implementation(projects.feature.favorite.impl)
    implementation(projects.feature.favorite.api)
    implementation(projects.feature.detail.impl)
    implementation(projects.feature.detail.api)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(projects.core.networkMock)
    mockImplementation(projects.core.networkMock)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)

    kspAndroidTest(libs.hilt.compiler)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.model)
    androidTestImplementation(projects.core.network)
    androidTestImplementation(projects.core.networkMock)
}
