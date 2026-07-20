package com.yiwenliu.tmdb

import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

internal fun Project.configureBuildConfigSecrets(keys: ListProperty<String>) {
    extensions.getByType<LibraryAndroidComponentsExtension>().apply {
        finalizeDsl { library -> library.buildFeatures.buildConfig = true }
        onVariants { variant ->
            keys.get().forEach { key ->
                variant.buildConfigFields?.put(
                    key,
                    buildConfigStringValue(key).map {
                        BuildConfigField("String", "\"$it\"", null)
                    },
                )
            }
        }
    }
}

private fun Project.buildConfigStringValue(key: String): Provider<String> =
    providers.of(LocalPropertyValueSource::class.java) {
        parameters.localProperties.set(
            rootProject.layout.projectDirectory.file("local.properties"),
        )
        parameters.key.set(key)
    }
        .orElse(providers.environmentVariable(key))
        .orElse("")
        .map { it.trim() }

abstract class LocalPropertyValueSource :
    ValueSource<String, LocalPropertyValueSource.Params> {
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
    }
}
