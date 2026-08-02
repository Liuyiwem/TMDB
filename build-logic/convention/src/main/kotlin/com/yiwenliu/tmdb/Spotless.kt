package com.yiwenliu.tmdb

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * ktlint 的 .editorconfig 覆寫，套用在所有 kotlin / kotlinGradle 區塊上。
 *
 * `no-unused-imports` 必須顯式開啟：`NoUnusedImportsRule` 實作了
 * `Rule.OnlyWhenEnabledInEditorconfig`，預設是關閉的。開啟後 `spotlessApply` 會自動移除
 * 未使用的 import。
 *
 * 兩個不明顯的陷阱：
 * 1. 這條規則帶 `@Deprecated`，將在 ktlint 2.0.0 移除，理由是 "When the rule marks an
 *    import falsely as unused, it results in code that can not be compiled."。把
 *    `libs.versions.toml` 的 ktlint 升到 2.x 會【無聲地】失去這條規則，因為 ktlint 會忽略
 *    無法辨識的 `ktlint_*` 屬性。也因為上游承認的失敗模式是「產生無法編譯的程式碼」，
 *    改動這份設定後必須真的編譯過，不能只看 `spotlessCheck` 綠燈。
 * 2. 它同時實作 `IgnoreKtlintSuppressions`，所以
 *    `@file:Suppress("ktlint:standard:no-unused-imports")` 是無效的。真的出現誤判時，
 *    唯一的逃生口是把這個屬性搬到 .editorconfig 並用較窄的 glob 設成 `disabled`，
 *    或使用 `targetExclude`。
 */
private val ktlintEditorConfig = mapOf(
    "android" to "true",
    "ktlint_standard_no-unused-imports" to "enabled",
)

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
            ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfig)
            endWithNewline()
            trimTrailingWhitespace()
        }
        kotlinGradle {
            target("*.kts", "build-logic/**/*.kts")
            targetExclude("**/build/**/*.kts")
            ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfig)
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
            ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfig)
            endWithNewline()
            trimTrailingWhitespace()
        }
        kotlinGradle {
            target("*.kts")
            targetExclude("**/build/**/*.kts")
            ktlint(ktlintVersion).editorConfigOverride(ktlintEditorConfig)
            endWithNewline()
            trimTrailingWhitespace()
        }
    }
}
