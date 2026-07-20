import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.yiwenliu.tmdb.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.tmdb.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.tmdb.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplicationFlavors") {
            id = libs.plugins.tmdb.android.application.flavors.get().pluginId
            implementationClass = "AndroidApplicationFlavorsConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.tmdb.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.tmdb.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("lint") {
            id = libs.plugins.tmdb.lint.get().pluginId
            implementationClass = "LintConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.tmdb.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.tmdb.android.hilt.get().pluginId
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidFeatureImpl") {
            id = libs.plugins.tmdb.android.feature.impl.get().pluginId
            implementationClass = "AndroidFeatureImplConventionPlugin"
        }
        register("androidFeatureApi") {
            id = libs.plugins.tmdb.android.feature.api.get().pluginId
            implementationClass = "AndroidFeatureApiConventionPlugin"
        }
        register("ktlint") {
            id = libs.plugins.tmdb.ktlint.get().pluginId
            implementationClass = "KtlintConventionPlugin"
        }
        register("androidBuildConfigSecrets") {
            id = libs.plugins.tmdb.android.buildConfig.secrets.get().pluginId
            implementationClass = "BuildConfigSecretsConventionPlugin"
        }
    }
}