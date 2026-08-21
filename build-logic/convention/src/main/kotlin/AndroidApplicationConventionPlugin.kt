import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.yiwenliu.tmdb.configureGradleManagedDevices
import com.yiwenliu.tmdb.configureJacoco
import com.yiwenliu.tmdb.configureKotlinAndroid
import com.yiwenliu.tmdb.configureSpotlessForAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.testing.jacoco.plugins.JacocoPlugin

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "tmdb.lint")
            apply<JacocoPlugin>()
            configureSpotlessForAndroid()

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 36
                @Suppress("UnstableApiUsage")
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)
                buildTypes {
                    debug {
                        enableUnitTestCoverage = true
                    }
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }

            extensions.configure<ApplicationAndroidComponentsExtension> {
                configureJacoco(this)
            }
        }
    }
}
