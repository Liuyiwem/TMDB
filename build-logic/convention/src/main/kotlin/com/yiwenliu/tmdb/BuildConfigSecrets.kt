package com.yiwenliu.tmdb

import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

internal fun Project.configureBuildConfigSecrets(keys: ListProperty<String>) {
    extensions.getByType<LibraryAndroidComponentsExtension>().apply {
        finalizeDsl { library -> library.buildFeatures.buildConfig = true }
        onVariants { variant ->
            keys.get().forEach { key ->
                val value = requiredSecret(key)
                variant.buildConfigFields?.put(
                    key,
                    provider { BuildConfigField("String", "\"${value.escapedForJavaLiteral()}\"", null) },
                )
            }
        }
    }
}

private fun Project.requiredSecret(key: String): String = providers.of(LocalPropertyValueSource::class.java) {
    parameters.localProperties.set(
        rootProject.layout.projectDirectory.file("local.properties"),
    )
    parameters.key.set(key)
}.orElse(
    providers.environmentVariable(key).map { it.trim() }.filter { it.isNotEmpty() },
).orNull?.also { validateSecret(key, it) } ?: error(
    "Missing build secret `$key`. Add `$key=<value>` to local.properties, " +
        "or export $key as an environment variable.",
)

private fun validateSecret(key: String, value: String) {
    if (!key.endsWith("_URL")) return
    if (!value.startsWith("https://")) {
        error("Build secret `$key` must start with `https://`, but was `$value`.")
    }
    if (!value.endsWith("/")) {
        error("Build secret `$key` must end with `/`, but was `$value`.")
    }
}

private fun String.escapedForJavaLiteral(): String = replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")

abstract class LocalPropertyValueSource : ValueSource<String, LocalPropertyValueSource.Params> {
    interface Params : ValueSourceParameters {
        val localProperties: RegularFileProperty
        val key: Property<String>
    }

    override fun obtain(): String? {
        val file = parameters.localProperties.asFile.get()
        if (!file.exists()) return null
        return Properties()
            .apply { file.inputStream().use { load(it) } }
            .getProperty(parameters.key.get())
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
