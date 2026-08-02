plugins {
    alias(libs.plugins.tmdb.android.application)
    alias(libs.plugins.tmdb.android.application.compose)
    alias(libs.plugins.tmdb.android.application.flavors)
    alias(libs.plugins.tmdb.android.hilt)
}

android {
    namespace = "com.yiwenliu.tmdb"

    defaultConfig {
        applicationId = "com.yiwenliu.tmdb"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.yiwenliu.core.data.test.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    implementation(projects.core.ui)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.home.api)
    implementation(projects.feature.search.impl)
    implementation(projects.feature.search.api)
    implementation(projects.feature.favorite.impl)
    implementation(projects.feature.favorite.api)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.ui.test.manifest)

    kspAndroidTest(libs.hilt.compiler)

    // 不可刪。看起來多餘（androidTest 原始碼一行都沒 import 它們，ui-test-junit4 也相依
    // espresso），但這兩行的作用是【版本對齊】：ui-test-junit4 帶進來的 espresso 較舊，
    // 缺少 Android 15+ 的修正，會在 Espresso.onIdle() 拋
    // NoSuchMethodException: android.hardware.input.InputManager.getInstance。
    // 顯式宣告把 espresso 釘在版本目錄的 3.7.0。實測拿掉後 8 個 E2E 全滅。
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(projects.core.dataTest)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(libs.hilt.testing)
}
