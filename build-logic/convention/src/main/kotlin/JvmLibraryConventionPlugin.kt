import com.yiwenliu.tmdb.configureKotlinJvm
import com.yiwenliu.tmdb.configureSpotlessForJvm
import com.yiwenliu.tmdb.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "tmdb.lint")
            configureSpotlessForJvm()

            configureKotlinJvm()

            dependencies {
                "testImplementation"(libs.findLibrary("kotlin.test").get())
            }
        }
    }
}
