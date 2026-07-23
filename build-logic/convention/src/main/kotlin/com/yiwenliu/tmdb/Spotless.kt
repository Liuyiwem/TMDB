package com.yiwenliu.tmdb

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

internal fun Project.configureSpotlessForAndroid() {
    configureSpotlessCommon()
    extensions.configure<SpotlessExtension> {
        format("xml") {
            target("src/**/*.xml")
            targetExclude("**/build/**/*.xml")
            endWithNewline()
            trimTrailingWhitespace()
        }
    }
}

internal fun Project.configureSpotlessForJvm() {
    configureSpotlessCommon()
}

internal fun Project.configureSpotlessForRootProject() {
    apply(plugin = "com.diffplug.spotless")
    val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
    extensions.configure<SpotlessExtension> {
        kotlin {
            target("build-logic/convention/src/**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint(ktlintVersion).editorConfigOverride(mapOf("android" to "true"))
            endWithNewline()
            trimTrailingWhitespace()
        }
        format("kts") {
            target("*.kts", "build-logic/*.kts", "build-logic/convention/*.kts")
            targetExclude("**/build/**/*.kts")
            endWithNewline()
            trimTrailingWhitespace()
        }
    }
}

private fun Project.configureSpotlessCommon() {
    apply(plugin = "com.diffplug.spotless")
    val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
    extensions.configure<SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint(ktlintVersion).editorConfigOverride(mapOf("android" to "true"))
            endWithNewline()
            trimTrailingWhitespace()
        }
        format("kts") {
            target("src/**/*.kts")
            targetExclude("**/build/**/*.kts")
            endWithNewline()
            trimTrailingWhitespace()
        }
    }
}
