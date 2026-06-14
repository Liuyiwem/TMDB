import com.yiwenliu.tmdb.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "tmdb.android.library")
            apply(plugin = "tmdb.android.library.compose")
            apply(plugin = "tmdb.android.hilt")
            apply(plugin = "tmdb.ktlint")

            dependencies {
                "implementation"(libs.findLibrary("androidx-hilt-navigation-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            }
        }
    }
}