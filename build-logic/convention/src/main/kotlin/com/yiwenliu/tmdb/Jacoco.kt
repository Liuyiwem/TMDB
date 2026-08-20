package com.yiwenliu.tmdb

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.SourceDirectories
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/*_Factory.class",
    "**/*_MembersInjector.class",
    "**/*Module_*Factory.class",
    "**/*ComposableSingletons*.class",
)

private fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

internal fun Project.configureJacoco(androidComponentsExtension: AndroidComponentsExtension<*, *, *>) {
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().requiredVersion
    }

    androidComponentsExtension.onVariants { variant ->
        val objectFactory = objects
        val buildDirectory = layout.buildDirectory.get().asFile
        val allJars: ListProperty<RegularFile> = objectFactory.listProperty(RegularFile::class.java)
        val allDirectories: ListProperty<Directory> = objectFactory.listProperty(Directory::class.java)

        val reportTask = tasks.register(
            "create${variant.name.capitalize()}CoverageReport",
            JacocoReport::class,
        ) {
            classDirectories.setFrom(
                allJars,
                allDirectories.map { directories ->
                    directories.map { directory ->
                        objectFactory.fileTree().setDir(directory).exclude(coverageExclusions)
                    }
                },
            )
            reports {
                xml.required = true
                html.required = true
            }

            fun SourceDirectories.Flat?.toFilePaths(): Provider<List<String>> = this
                ?.all
                ?.map { directories -> directories.map { it.asFile.path } }
                ?: provider { emptyList() }

            sourceDirectories.setFrom(
                files(
                    variant.sources.java.toFilePaths(),
                    variant.sources.kotlin.toFilePaths(),
                ),
            )

            executionData.setFrom(
                objectFactory.fileTree()
                    .setDir("$buildDirectory/outputs/unit_test_code_coverage/${variant.name}UnitTest")
                    .matching { include("**/*.exec") },
                objectFactory.fileTree()
                    .setDir("$buildDirectory/outputs/code_coverage/${variant.name}AndroidTest")
                    .matching { include("**/*.ec") },
            )
        }

        variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
            .use(reportTask)
            .toGet(
                ScopedArtifact.CLASSES,
                { _ -> allJars },
                { _ -> allDirectories },
            )
    }

    tasks.withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}
